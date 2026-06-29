package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import shared.Doctor;
import shared.GameSettings;
import shared.Message;
import shared.MessageType;
import shared.Peasant;
import shared.Phase;
import shared.Role;
import shared.Seer;
import shared.Team;
import shared.Vampire;

public class GameManager {
    private GameRoom room;
    private GameSettings settings;
    private Phase currentPhase;

    private Map<ClientHandler, Role> roles = new HashMap<>();
    private Set<ClientHandler> alive = new HashSet<>();

    private Map<ClientHandler, ClientHandler> nightTargets = new HashMap<>();
    private Map<ClientHandler, ClientHandler> votes = new HashMap<>();

    private Thread phaseTimer;

    public GameManager(GameRoom room, GameSettings settings) {
        this.room = room;
        this.settings = settings;
        this.currentPhase = Phase.LOBBY;
    }
    
    public synchronized void requestPhaseSkip() {
        if (currentPhase != Phase.DISCUSSION) return;
        
        room.broadcast(new Message(MessageType.SYSTEM, "SERVER", "Host skipped to voting."));
        endCurrentPhase();
    }

    public Phase getCurrentPhase() { return currentPhase; }
    public Role getRole(ClientHandler p) { return roles.get(p); }
    public boolean isAlive(ClientHandler p) { return alive.contains(p); }

    public synchronized void startGame(ClientHandler requester) {
        if (currentPhase != Phase.LOBBY) {
            requester.sendMessage(new Message(MessageType.SYSTEM, "SERVER", "Game has already started."));
            return;
        }
        if (requester != room.getHost()) {
            requester.sendMessage(new Message(MessageType.SYSTEM, "SERVER", "Only the host can start the game."));
            return;
        }
        if (room.getPlayerCount() != settings.getPlayerCount()) {
            room.broadcast(new Message(MessageType.SYSTEM, "SERVER", "Need exactly " + settings.getPlayerCount() + " players (have " + room.getPlayerCount() + ")."));
            return;
        }

        assignRoles();
        startPhase(Phase.NIGHT);
    }
    
    public synchronized void handlePlayerLeave(ClientHandler leaver) {
        if (currentPhase == Phase.LOBBY || currentPhase == Phase.GAME_OVER) return;
        if (!roles.containsKey(leaver)) return;

        Role role = roles.get(leaver);
        boolean wasAlive = alive.contains(leaver);
        alive.remove(leaver);

        if (wasAlive) {
            String name = leaver.getPlayerName();
            room.broadcast(new shared.Message(MessageType.DEATH, "SERVER", name + " left the game and was revealed as a " + role.getName() + "."));

            broadcastPlayerList();

            if (checkWinConditions()) {
                return;
            }
        }
    }

    private void assignRoles() {
        List<Role> pool = new ArrayList<>();
        for (int i = 0; i < settings.getVampireCount(); i++) pool.add(new Vampire());
        for (int i = 0; i < settings.getSeerCount(); i++)    pool.add(new Seer());
        for (int i = 0; i < settings.getDoctorCount(); i++)  pool.add(new Doctor());
        for (int i = 0; i < settings.getPeasantCount(); i++) pool.add(new Peasant());

        Collections.shuffle(pool);

        List<ClientHandler> players = room.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            ClientHandler p = players.get(i);
            Role r = pool.get(i);
            roles.put(p, r);
            alive.add(p);
            p.sendMessage(new Message(MessageType.ROLE_ASSIGN, "SERVER", r.getName()));
        }

        revealVampiresToVampires();
        broadcastPlayerList();
    }

    private void revealVampiresToVampires() {
        for (ClientHandler recipient : roles.keySet()) {
            if (!(roles.get(recipient) instanceof Vampire)) continue;
            
            StringBuilder names = new StringBuilder();
            for (ClientHandler other : roles.keySet()) {
                if (other == recipient) continue;  
                if (roles.get(other) instanceof Vampire) {
                    if (names.length() > 0) names.append(", ");
                    names.append(other.getPlayerName());
                }
            }
            
            String revealText;
            if (names.length() == 0) {
                revealText = "You are the only vampire.";
            } else {
                revealText = "Your fellow vampires: " + names.toString();
            }
            recipient.sendMessage(new Message(MessageType.SYSTEM, "SERVER", revealText));
        }
    }

    private void broadcastPlayerList() {
        List<String> aliveNames = new ArrayList<>();
        for (ClientHandler p : room.getPlayers()) {
            if (alive.contains(p)) {
                aliveNames.add(p.getPlayerName());
            }
        }
        room.broadcast(new Message(MessageType.PLAYER_LIST, "SERVER",
            String.join(",", aliveNames)));
    }

    private ClientHandler findByName(String name) {
        for (ClientHandler p : room.getPlayers()) {
            if (name.equals(p.getPlayerName())) return p;
        }
        return null;
    }

    private void startPhase(Phase phase) {
        this.currentPhase = phase;
        nightTargets.clear();
        if (phase == Phase.VOTE) votes.clear();

        room.broadcast(new Message(MessageType.PHASE_CHANGE, "SERVER", phase.name()));

        int seconds = phaseDuration(phase);
        if (seconds > 0) scheduleNextPhase(phase, seconds);
    }

    private int phaseDuration(Phase phase) {
        switch (phase) {
            case NIGHT:      return settings.getNightSeconds();
            case DAY_REVEAL: return 5;
            case DISCUSSION: return settings.getDiscussionSeconds();
            case VOTE:       return settings.getVoteSeconds();
            default:         return 0;
        }
    }

    private void scheduleNextPhase(Phase fromPhase, int seconds) {
        if (phaseTimer != null) phaseTimer.interrupt();
        phaseTimer = new Thread(() -> {
            try {
                Thread.sleep(seconds * 1000L);
                synchronized (GameManager.this) {
                    if (currentPhase == fromPhase) {
                        endCurrentPhase();
                    }
                }
            } catch (InterruptedException e) {
            	
            }
        });
        phaseTimer.setDaemon(true);
        phaseTimer.start();
    }

    private void endCurrentPhase() {
        switch (currentPhase) {
            case NIGHT:
                resolveNight();
                if (!checkWinConditions()) startPhase(Phase.DAY_REVEAL);
                break;
            case DAY_REVEAL:
                startPhase(Phase.DISCUSSION);
                break;
            case DISCUSSION:
                startPhase(Phase.VOTE);
                break;
            case VOTE:
                resolveVote();
                if (!checkWinConditions()) startPhase(Phase.NIGHT);
                break;
            default:
                break;
        }
    }

    public synchronized void registerNightAction(ClientHandler actor, String targetName) {
        if (currentPhase != Phase.NIGHT) {
            actor.sendMessage(new Message(MessageType.SYSTEM, "SERVER", "You can only act at night."));
            return;
        }
        if (!alive.contains(actor)) return;

        Role r = roles.get(actor);
        if (r == null || !r.hasNightAction()) {
            actor.sendMessage(new Message(MessageType.SYSTEM, "SERVER", "You have no night action."));
            return;
        }

        ClientHandler target = findByName(targetName);
        if (target == null || !alive.contains(target)) {
            actor.sendMessage(new Message(MessageType.SYSTEM, "SERVER", "Invalid target: " + targetName));
            return;
        }

        if (r instanceof Doctor && target == actor) {
            actor.sendMessage(new Message(MessageType.SYSTEM, "SERVER", "Doctor cannot protect themselves."));
            return;
        }

        nightTargets.put(actor, target);
        actor.sendMessage(new Message(MessageType.SYSTEM, "SERVER", "Target locked in: " + targetName));
    }

    private void resolveNight() {
        Map<ClientHandler, Integer> vampVotes = new HashMap<>();
        for (Map.Entry<ClientHandler, ClientHandler> e : nightTargets.entrySet()) {
            if (roles.get(e.getKey()) instanceof Vampire && e.getValue() != null) {
                vampVotes.merge(e.getValue(), 1, Integer::sum);
            }
        }
        ClientHandler killTarget = null;
        int maxVampVotes = 0;
        for (Map.Entry<ClientHandler, Integer> e : vampVotes.entrySet()) {
            if (e.getValue() > maxVampVotes) {
                killTarget = e.getKey();
                maxVampVotes = e.getValue();
            }
        }

        ClientHandler protectedPlayer = null;
        for (Map.Entry<ClientHandler, ClientHandler> e : nightTargets.entrySet()) {
            if (roles.get(e.getKey()) instanceof Doctor) {
                protectedPlayer = e.getValue();
                break;
            }
        }

        for (Map.Entry<ClientHandler, ClientHandler> e : nightTargets.entrySet()) {
            ClientHandler actor = e.getKey();
            ClientHandler target = e.getValue();
            if (roles.get(actor) instanceof Seer && target != null) {
                Team t = roles.get(target).getTeam();
                actor.sendMessage(new Message(MessageType.INVESTIGATION_RESULT, "SERVER", target.getPlayerName() + ":" + t.name()));
            }
        }

        if (killTarget != null && killTarget != protectedPlayer) {
            alive.remove(killTarget);
            room.broadcast(new Message(MessageType.DEATH, "SERVER", killTarget.getPlayerName() + " was killed during the night."));
        } else if (killTarget != null) {
            room.broadcast(new Message(MessageType.SYSTEM, "SERVER", "The doctor saved a life last night."));
        } else {
            room.broadcast(new Message(MessageType.SYSTEM, "SERVER", "Nobody died last night."));
        }

        broadcastPlayerList();
    }
    
    public synchronized void registerVote(ClientHandler voter, String targetName) {
        if (currentPhase != Phase.VOTE) {
            voter.sendMessage(new Message(MessageType.SYSTEM, "SERVER", "Voting isn't open."));
            return;
        }
        if (!alive.contains(voter)) return;

        ClientHandler target = findByName(targetName);
        if (target == null || !alive.contains(target)) {
            voter.sendMessage(new Message(MessageType.SYSTEM, "SERVER", "Invalid target: " + targetName));
            return;
        }

        votes.put(voter, target);
        
        if (votes.size() >= alive.size()) {
            endCurrentPhase();
        }
        
        room.broadcast(new Message(MessageType.SYSTEM, "SERVER", "(" + votes.size() + "/" + alive.size() + " votes cast)"));
    }

    private void resolveVote() {
        Map<ClientHandler, Integer> counts = new HashMap<>();
        for (ClientHandler target : votes.values()) {
            counts.merge(target, 1, Integer::sum);
        }

        ClientHandler eliminated = null;
        int maxCount = 0;
        boolean tied = false;
        for (Map.Entry<ClientHandler, Integer> e : counts.entrySet()) {
            if (e.getValue() > maxCount) {
                eliminated = e.getKey();
                maxCount = e.getValue();
                tied = false;
            } else if (e.getValue() == maxCount) {
                tied = true;
            }
        }

        if (eliminated == null) {
            room.broadcast(new Message(MessageType.SYSTEM, "SERVER", "No votes were cast."));
        } else if (tied) {
            room.broadcast(new Message(MessageType.SYSTEM, "SERVER", "The vote was tied. Nobody was eliminated."));
        } else {
            alive.remove(eliminated);
            Role r = roles.get(eliminated);
            room.broadcast(new Message(MessageType.VOTE_RESULT, "SERVER", eliminated.getPlayerName() + " was eliminated. They were a " + r.getName() + "."));
        }

        broadcastPlayerList();
    }

    private boolean checkWinConditions() {
        int vampires = 0, villagers = 0;
        for (ClientHandler p : alive) {
            if (roles.get(p).getTeam() == Team.VAMPIRE) vampires++;
            else villagers++;
        }

        if (vampires == 0) {
            endGame(Team.VILLAGE);
            return true;
        }
        if (vampires >= villagers) {
            endGame(Team.VAMPIRE);
            return true;
        }
        return false;
    }

    private void endGame(Team winner) {
        currentPhase = Phase.GAME_OVER;
        if (phaseTimer != null) phaseTimer.interrupt();

        StringBuilder reveal = new StringBuilder();
        boolean first = true;
        for (ClientHandler p : room.getPlayers()) {
            Role r = roles.get(p);
            if (r != null) {
                if (!first) reveal.append(",");
                reveal.append(p.getPlayerName()).append("=").append(r.getName());
                first = false;
            }
        }
        room.broadcast(new Message(MessageType.ROLE_REVEAL, "SERVER", reveal.toString()));
        room.broadcast(new Message(MessageType.GAME_OVER, "SERVER", winner.name()));
    }
}