package com.ahmet.eldrctestapplication.communication;

/**
 * Author: Ahmet TOPAK
 * Since: 10/30/2024
 */
public class RobotMessageBuilder {

    private static int packetId = 1;

    // Enum for Transmitter IDs
    public enum Transmitter {
        MOTOR_CONTROLLER('C'),
        MAIN_BOARD('M'),
        MASTER('X');

        private final char id;
        Transmitter(char id) { this.id = id; }
        public char getId() { return id; }
    }

    // Enum for Receiver IDs
    public enum Receiver {
        ALL('A'),
        MOTOR_CONTROLLER('C'),
        MAIN_BOARD('M'),
        MASTER('X');

        private final char id;
        Receiver(char id) { this.id = id; }
        public char getId() { return id; }
    }

    // Enum for Process Types
    public enum Process {
        WRITE('!'),
        ACK('*'),
        READ('?'),
        ANSWER('^'),
        MESSAGE('#');

        private final char code;
        Process(char code) { this.code = code; }
        public char getCode() { return code; }
    }

    // Enum for Components
    public enum Component {
        MOTOR_DRIVE('A'),
        MOTOR_PAL('B'),
        MOTOR_ARM('C'),
        MOTOR_CLAMP('D'),
        MOTOR_PTZ('E'),
        MOTOR_ADD('F'),
        LED_HEADLIGHT('G'),
        LED_LAMP('H'),
        OUT_BRAKE('I'),
        OUT_POWER('J');

        private final char code;
        Component(char code) { this.code = code; }
        public char getCode() { return code; }
    }

    // Enum for Indexes
    public enum Index {
        INDEX_1('1'), INDEX_2('2'), INDEX_3('3'),
        INDEX_4('4'), INDEX_5('5'), INDEX_6('6'),
        ALL('A');

        private final char code;
        Index(char code) { this.code = code; }
        public char getCode() { return code; }
    }

    // Packet ID generation
    private static String getPacketId() {
        if (packetId > 999) {
            packetId = 1;
        }
        return String.format("%03d", packetId++);
    }

    // Builds packet according to protocol with specified parameters
    public static String buildPacket(
            Transmitter transmitter, Receiver receiver, Process process,
            Component component, Index index, int[] data) {

        String packetIdStr = getPacketId();
        StringBuilder packet = new StringBuilder(packetIdStr)
                .append(transmitter.getId()).append(receiver.getId()).append(process.getCode())
                .append(component.getCode()).append(index.getCode()).append("[");

        for (int i = 0; i < data.length; i++) {
            packet.append(data[i]);
            if (i < data.length - 1) {
                packet.append(":");
            }
        }
        packet.append("]");
        return packet.toString();
    }

    // Method for sending only motor speeds
    public static String createMotorSpeedPacket(
            Transmitter transmitter, Receiver receiver, Component component,
            int speedFL, int speedFR, int speedRL, int speedRR, int speedML, int speedMR) {

        int[] motorSpeeds = {speedFL, speedFR, speedRL, speedRR, speedML, speedMR};
        return buildPacket(transmitter, receiver, Process.WRITE, component, Index.ALL, motorSpeeds);
    }

    // Specialized methods for other packet types
    public static String createWritePacket(
            Transmitter transmitter, Receiver receiver, Component component,
            Index index, int parameter, int value) {

        return buildPacket(transmitter, receiver, Process.WRITE, component, index,
                new int[]{parameter, value});
    }

    public static String createReadPacket(
            Transmitter transmitter, Receiver receiver, Component component, Index index, int parameter) {

        return buildPacket(transmitter, receiver, Process.READ, component, index, new int[]{parameter});
    }

    public static String createAckPacket(
            Transmitter transmitter, Receiver receiver, Component component,
            Index index, int parameter, int errorCode) {

        return buildPacket(transmitter, receiver, Process.ACK, component, index, new int[]{parameter, errorCode});
    }

    public static String createAnswerPacket(
            Transmitter transmitter, Receiver receiver, Component component,
            Index index, int parameter, int value) {

        return buildPacket(transmitter, receiver, Process.ANSWER, component, index, new int[]{parameter, value});
    }

    public static String createMessagePacket(
            Transmitter transmitter, Receiver receiver, Component component,
            Index index, int parameter, String message) {

        int[] messageData = new int[]{parameter}; // Modify to encode message as data if needed
        return buildPacket(transmitter, receiver, Process.MESSAGE, component, index, messageData);
    }
}
