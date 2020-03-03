package ServerSoft;

import Answers.ServerAnswer;
import Parser.JSONParser;
import PlantsInfo.Plants;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class ConsoleReader {

//    private int PORT_CLIENT = 3333;
    private  int[] ports = new int[5];
    private int count = 0;
    private int PORT_SERVER;
    private Sender serversender;
    private InetAddress inetAddress;
    private Scanner scanner;
    private ManagerForPlants managerForPlants;
    private DatagramSocket datagramSocket;
    private boolean make =false;
    private InetAddress clientAdress;
    private boolean connected = false;
    private boolean portSet = false;

    public void setMake(boolean make) {
        this.make = make;
    }

    public boolean getMake(){
        return make;
    }

    public ConsoleReader() throws IOException{
        scanner = new Scanner(System.in);
        inetAddress = InetAddress.getByName("localhost");
    }

    public void setPorts(int port) {
        try {
            this.ports[count] = port;
            count++;
        }catch (ArrayIndexOutOfBoundsException e){
        }
    }

    public void setConnected(boolean connected){
        this.connected = connected;
    }

    protected void setPort(){
        portSet = false;
        System.out.println("----\nЗапук сервера\n----");
        while (!portSet){
            try {
                String numb = scanner.nextLine();
                System.out.println("----");
                if (numb.matches("[0-9]+")) {
                    if (Integer.parseInt(numb) < 65535) {
                        PORT_SERVER = Integer.parseInt(numb);
                        datagramSocket = new DatagramSocket(PORT_SERVER);
                        clientAdress = InetAddress.getByName("localhost");
                        portSet = true;
                    }else {
                        System.out.println("----\nНедопустимый номер порта, введите снова\n----");
                        continue;
                    }
                }else {
                    System.out.println("----\nНедопустимый номер порта, введите снова\n----");
                    continue;
                }
            }catch (IOException e){
                System.out.println("----\nНедопустимый номер порта, введите снова\n----");
                continue;
            }
        }

    }


    private void shootDownHook(){
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                managerForPlants.saving();
//                for (int i : ports) {
//                    send(new ServerAnswer("", "DISCONNECT"), ports[i]);
//                    System.out.println("----\nЗавершение работы\n----");
//                }
            }));
        }catch (ArrayIndexOutOfBoundsException e){}
    }

    public void send(ServerAnswer serverAnswer,int port){
        serversender.send(serverAnswer,port);
    }

    public void work() throws InterruptedException{
        System.out.println("----\nСтарт работы.\n----");
        setPort();
        serversender = new Sender(datagramSocket,clientAdress);
        serversender.start();
        managerForPlants = new ManagerForPlants(this);
        Receiver receiver = new Receiver(datagramSocket,this);
        receiver.setDaemon(true);
        receiver.start();
        shootDownHook();
        while (true){

        }
    }
//    public void makeManager(){
//            managerForPlants = new ManagerForPlants(this);
//            managerForPlants.setWork(true);
//    }

//    public void closeManager(){
//        if(connected) {
//            if (managerForPlants.getWork()) {
//                managerForPlants.saveAndExit();
//                managerForPlants.exit();
//            }
//        }
//    }

    public ManagerForPlants getManagerForPlants() {
        return managerForPlants;
    }

    public String help(){
        return ("----\nСписок доступных команд:\n" +
                "1. help: показать доступные команды.\n" +
                "2. remove {element}: удалить элемент из коллекции по его значению.\n" +
                "3. info: вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.), элемент должен быть введен в формате json.\n" +
                "4. remove_lower {element}: удалить из коллекции все элементы, меньше заданного, элемент должен быть введён в формате json.\n" +
                "5. add {element}: добавить новый элемент в коллекцию, элемент должен быть введён в формате json.\n" +
                "6. add_if_max {element}: добавить новый элемент в коллекцию, если он превышает максимальный, элемент должен быть введён в формате json.\n" +
                "7. remove_greater {element}: удалить из коллекции все элементы, превышающие заданный, элемент должен быть введён в формате json.\n" +
                "8. show: вывести в стандартный поток вывода все элементы коллекции в строковом представлении.\n" +
                "9. exit: закончить работу.\n" +
                "10. import: загрузка необходимых данных с клиента.\n" +
                "11. load: загрузка необходимых данных с сервера.\n" +
                "12. save: сохранение коллекции на сервере.\n" +
                "Пример элемента ввода:\n" +
                "{\n" +
                "  \"name\": \"Ромашка\",\n" +
                "  \"characteristic\": \"White\",\n" +
                "  \"location\":{\n" +
                "  \t\"namelocation\": \"Поле\"\n" +
                "  }\n" +
                "}\n" +
                "----");
    }

    private int getBracket(String str,char bracket){
        int count = 0;
        for(char c : str.toCharArray()){
            if (c == bracket)
                count++;
        }
        return count;
    }

}
