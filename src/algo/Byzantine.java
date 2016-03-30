package algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import syssim.Participant;
import syssim.SimSystem;
import syssim.SysSimException;

public class Byzantine {
    private static SimSystem system;
    private static List<Participant> participants = new ArrayList<Participant>();
    private static Map<Integer, Integer> participantIdList = new HashMap<Integer, Integer>();
    private static Hashtable<Integer, String> finalStatus = new Hashtable<Integer, String>();

    public static class ElectionListener implements Participant.EventListener {
        final List<String> tokens = new ArrayList<String>();
        private int participantCount;
        private int participantIndex;
        private NODE_STATUS faithType;
        private ROLE role;
        private int attackCount;
        private int retreatcount;
        public boolean init;

        public ElectionListener(int participantCount, int participantIndex, NODE_STATUS faithType, ROLE role) {
            this.participantCount = participantCount;
            this.participantIndex = participantIndex;
            this.faithType = faithType;
            this.role = role;
        }

        @Override
        public void participantStarted(SimSystem simSystem) {
            String command = "ATTACK";
            if (ROLE.SOURCE == role) {
                participants.remove(0);
                System.out.println("Role : [Node 0 - " + role + "], Faith : [" + faithType + "]");
                for (int index = 0; index < participants.size(); index++) {
                    command = NODE_STATUS.FAULTY.equals(faithType) ? getCommand(index) : command;
                    simSystem.sendMessage(participants.get(index).getID(), new String[] { command });
                    System.out.println("[Node 0 - " + role + "], Sending : [" + command + "] to [Node "
                                       + participantIdList.get(participants.get(index).getID()) + "]");

                }
            } else {
                System.out.println("Role : [" + role + " " + participantIndex + "], Faith : [" + faithType + "]");
            }
        }

        private String getCommand(final int index) {
            if (index % 2 == 1) {
                return "RETREAT";
            }
            return "ATTACK";
        }

        @Override
        public void eventReceived(SimSystem simSystem, String[] event) {
            if (ROLE.SOURCE == role) {
                return;
            }
            System.out.println("[Node " + participantIndex + "] received " + Arrays.toString(event));
            tokens.add(event[0]);
            if (event[0].equals("ATTACK")) {
                attackCount++;
            } else {
                retreatcount++;
            }
            if (!init) {
                alertOtherNodes(simSystem, event[0]);
                init = true;
            }

            if (tokens.size() == participantCount - 1) {
                StringBuilder builder = new StringBuilder("[Node " + participantIndex + "] Received Commands : [");
                for (String token : tokens) {
                    builder.append(token);
                    builder.append(" ");
                }
                String decision = "";

                if (attackCount > retreatcount) {
                    decision = "ATTACK";
                } else if (retreatcount > attackCount) {
                    decision = "RETREAT";
                } else {
                    decision = "NO CONSENSUS";
                }
                builder.append("] ---> Decision [" + decision + "]");
                System.out.println(builder.toString());
                finalStatus.put(participantIndex, decision);
            }
        }

        private void alertOtherNodes(SimSystem simSystem, String recievedCommand) {
            List<Participant> localParticipants = new ArrayList<Participant>();
            localParticipants.addAll(participants);
            localParticipants.remove(participantIndex - 1);
            String command;
            for (int index = 0; index < localParticipants.size(); index++) {
                command = NODE_STATUS.FAULTY.equals(faithType) ? getCommand(index) : recievedCommand;
                simSystem.sendMessage(localParticipants.get(index).getID(), new String[] { command });
                System.out.println("[" + role + " " + participantIndex + "], Sending : [" + command + "] to [Node "
                                   + participantIdList.get(localParticipants.get(index).getID()) + "]");
            }

        }
    }

    private static void setupWithToatlAndTraitorCount(final int participantCount, int traitorCount) throws SysSimException {
        System.out.println("System starting with [" + participantCount + "] Nodes with [" + traitorCount
                           + "] faulty node(s), [Node 0] is the Source.");
        List<Integer> traitors = randomizeTraitor(participantCount, traitorCount);
        for (int i = 0; i < participantCount; i++) {
            NODE_STATUS faith = NODE_STATUS.PERFECT;
            final int participantIndex = i;
            if (traitors.contains(i)) {
                faith = NODE_STATUS.FAULTY;
            }
            Participant participant = system.createParticipant(new ElectionListener(participantCount, participantIndex, faith, i == 0 ? ROLE.SOURCE
                                                                                                                                     : ROLE.NODE));
            participants.add(participant);
            participantIdList.put(participant.getID(), i);
        }
        system.bootUp();
    }

    private static List<Integer> randomizeTraitor(int pCount, int traitorCount) {
        Random random = new Random();
        List<Integer> traitorList = new ArrayList<Integer>();
        for (int i = 0; i < traitorCount; i++) {
            traitorList.add(random.nextInt(pCount));
        }
        return traitorList;
    }

    public enum NODE_STATUS {
        PERFECT("Perfect"), FAULTY("Faulty");

        private String faithType;

        private NODE_STATUS(String s) {
            faithType = s;
        }

        public String getNodeStatus() {
            return faithType;
        }

    }

    public enum ROLE {
        SOURCE("Coordinator"), NODE("Node");

        private String role;

        private ROLE(String s) {
            role = s;
        }

        public String getRole() {
            return role;
        }
    }

    public enum COMMAND {
        ATTACK(1), RETREAT(0);

        private int command;

        private COMMAND(int com) {
            command = com;
        }

        public int getCommand() {
            return command;
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the number of Generals (N) and Traitors (M):");
        int NUM_GENERALS = sc.nextInt();
        int NUM_TRAITORS = sc.nextInt();
        system = new SimSystem();
        setupWithToatlAndTraitorCount(NUM_GENERALS, NUM_TRAITORS);
        Thread.sleep(3000);
        System.out.println("---------------------------------------------------------------------------");

        if (finalStatus.containsValue("NO CONSENSUS")) {
            System.out.println("NO CONSENSUS REACHED");
        } else {
            System.out.println("CONSENSUS REACHED");
        }

    }
}
