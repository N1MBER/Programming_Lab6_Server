package ServerSoft;

import Answers.ClientAnswer;
import Answers.ServerAnswer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Receiver extends Thread {

    private DatagramSocket socket;
    private ConsoleReader reader;

    public Receiver(DatagramSocket datagramSocket, ConsoleReader consoleReader){
        this.socket = datagramSocket;
        this.reader = consoleReader;
    }

    @Override
    public void run(){
        while (!isInterrupted()){
            try {
                byte[] bytes = new byte[16384];
                DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length);
                socket.receive(datagramPacket);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                ClientAnswer clientAnswer = (ClientAnswer) objectInputStream.readObject();
                System.out.println(clientAnswer.getAnswer());
                new Analyzator(reader,clientAnswer,datagramPacket.getPort());
                reader.setPorts(datagramPacket.getPort());
                byteArrayInputStream.close();
                objectInputStream.close();

            }catch (IOException | ClassNotFoundException e){
                System.out.println("----\nВозникла ошибка:\n");
                e.printStackTrace();
            }
        }
    }

}
