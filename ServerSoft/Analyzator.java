package ServerSoft;

import Answers.ClientAnswer;
import Answers.ServerAnswer;

public class Analyzator {

    private ClientAnswer answer;
    private int port;
    private ConsoleReader reader;

    public Analyzator(ConsoleReader consoleReader,ClientAnswer clientAnswer,  int port){
        this.answer = clientAnswer;
        this.port =port;
        this.reader = consoleReader;
        analyz(reader,answer);
    }

    public void analyz(ConsoleReader reader, ClientAnswer answer){
        try {
            switch (answer.getAnswer()){
                case "CONNECT":
                    reader.setConnected(true);
                    reader.send(new ServerAnswer("", "CONNECT"),port);
                    Thread.sleep(1000);
                    reader.send(new ServerAnswer(reader.help(), "HELP"),port);
                    break;
                case "ADD":
                    reader.send(new ServerAnswer(reader.getManagerForPlants().add(answer.getPlant()), "ADD"),port);
                    break;
                case "ADD_IF_MAX":
                    reader.send(new ServerAnswer(reader.getManagerForPlants().add_if_max(answer.getPlant()), "ADD_IF_MAX"),port);
                    break;
                case "REMOVE":
                    reader.send(new ServerAnswer(reader.getManagerForPlants().remove(answer.getPlant()), "REMOVE"),port);
                    break;
                case "IMPORT":
                    reader.send(new ServerAnswer(reader.getManagerForPlants().importCollection(answer.getPlants()),"IMPORT"),port);
                    break;
                case "REMOVE_LOWER":
                    reader.send(new ServerAnswer(reader.getManagerForPlants().remove_lower(answer.getPlant()), "REMOVE_LOWER"),port);
                    break;
                case "REMOVE_GREATER":
                    reader.send(new ServerAnswer(reader.getManagerForPlants().remove_greater(answer.getPlant()), "REMOVE_GREATER"),port);
                case "INFO":
                    reader.send(new ServerAnswer(reader.getManagerForPlants().info(), "INFO"),port);
                    break;
                case "SHOW":
                    reader.send(new ServerAnswer(reader.getManagerForPlants().show(), "SHOW"),port);
                    break;
                case "LOAD":
                    reader.getManagerForPlants().loadData(port);
                    reader.getManagerForPlants().clearAnswerOfLOAD();
                    reader.getManagerForPlants().clearAnswerOfSave();
                    break;
                case "LOAD_PATH":
                    reader.getManagerForPlants().loadPathData(answer.getPaths());

                    reader.send(new ServerAnswer(reader.getManagerForPlants().getAnswerOfLOAD(), "LOAD_TRUE"),port);
                    reader.getManagerForPlants().clearAnswerOfSave();
                case "SAVE":
                    reader.getManagerForPlants().saveAndExit();
                    reader.send(new ServerAnswer(reader.getManagerForPlants().getAnswerOfSave(), "SAVE"),port);
                    reader.getManagerForPlants().clearAnswerOfSave();
                    break;
                case "HELP":
                    reader.help();
                    reader.send(new ServerAnswer(reader.help(), "HELP"),port);
                    break;
                default:
                    System.out.println("----\nНераспознанный ответ.\n----");
                    break;
            }
        }catch (InterruptedException e) {
            System.out.println("----\nВозникла ошибка:");
            e.printStackTrace();
        }
    }
}
