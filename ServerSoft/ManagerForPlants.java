package ServerSoft; /**
 * @author Makes
 * This class reads information from file, remakes it in LinkedHashSet and has some functions for managment.
 * The path of file contains in environment variable with name "COLLECTION_PATH".
 */


import Answers.ServerAnswer;
import Parser.CSVWriter;
import PlantsInfo.Plants;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;

public class ManagerForPlants {

    private String answerOfLOAD = "";
    private String answerOfSave = "";
    private ConsoleReader reader;
    private boolean setPath = false;
    /**
     * boolean isWork - values of the need to work
     */
    private boolean isWork = false;
    private ConcurrentSkipListSet<Plants> plants;
    /**
     * Date date - date now
     */
    private Date date;
    /**
     * File sourceFile - file for reading or writing
     */
    private File sourceFile;
    public String textOfFile;

    private boolean IsCSV = false;

    private String getFilePath(int port){
        String path = System.getenv("PATH_CSV");
        if (path == null){
            answerOfLOAD = ("----\nПуть через переменную окружения PATH_CSV не указан!\nНапишите адрес вручную(в консоль)\n----");
            reader.send(new ServerAnswer(answerOfLOAD,"LOAD_ERROR"),port);
            setPath =false;
            return path;
        }
        else {
            setPath = true;
            return path;
        }
    }

    public void clearAnswerOfSave(){
        answerOfSave = "";
    }

    public void clearAnswerOfLOAD() {
        answerOfLOAD = "";
    }

    public String importCollection(ConcurrentSkipListSet<Plants> additionalColl) {
        plants.addAll(additionalColl);
        date = new Date();
        return ("----\nУспешно добавлено " + additionalColl.size() + " элементов.\n----");
    }

    protected ManagerForPlants(ConsoleReader consoleReader) {
        this.reader = consoleReader;
        plants = new ConcurrentSkipListSet<Plants>();
        sourceFile = null;
    }

    public void loadData(int port) {
        String path = getFilePath(port);
        if (setPath) {
            sourceFile = new File(path);
            readFile(sourceFile);
            reader.send(new ServerAnswer(reader.getManagerForPlants().getAnswerOfLOAD(),"LOAD_TRUE"),port);
        }
    }

    public void loadPathData(String path){
        clearAnswerOfLOAD();
        System.out.println("---\n" + answerOfLOAD + "\n----");
        sourceFile = new File(path.trim());
        readFile(sourceFile);
    }

    public String readCSV(File file) {
        String txt = "";
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNext()) {
                txt += scanner.nextLine();
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                saveAndExit();
            }));
            answerOfLOAD+= ("----\nФайл считан\n----");
        } catch (IOException e) {
            answerOfLOAD+=("----\nОшибка чтения файла.\nРабота невозможна.\n----");
        }
        try {
            FileWriter fileWrite = new FileWriter(file);
            fileWrite.write(txt);
        } catch (IOException e) {
            e.getMessage();
        }
        //AfterReadSave(txt);
        this.textOfFile = txt;
        return txt;
    }

    public void setWork(boolean work) {
        isWork = work;
    }

    public boolean getWork() {
        return isWork;
    }

    protected void BeforeSaveDelete(File file) {
        String txt = "";
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNext()) {
                txt += scanner.nextLine();
            }
        } catch (IOException e) {
        }
    }

    /**
     * This method analysis information about file and send file to method JSONtoLHS
     *
     * @param impFile - analyzed file
     */
    public void readFile(File impFile) {
        date = new Date();
        try {
            if (!(impFile.isFile())) {
                answerOfLOAD += ("----\nПуть ведёт не к файлу.\nРабота невозможна.\n----");
                throw new FileNotFoundException("----\nПуть ведёт не к файлу.\nРабота невозможна.\n----");
            }
            if (!(impFile.exists())) {
                answerOfLOAD += ("----\nПо указанному пути файл не найден.\nДальнейшая работа невозможна.\n----");
                throw new FileNotFoundException("----\nПо указанному пути файл не найден.\nДальнейшая работа невозможна.\n----");
            }
            if (!(impFile.canRead())) {
                answerOfLOAD += ("----\nНет прав на чтение.\nДальнейшая работа невозможна.\n----");
                throw new SecurityException("----\nНет прав на чтение.\nДальнейшая работа невозможна.\n----");
            }
            CSVtoLHS(readCSV(impFile));
        } catch (FileNotFoundException | SecurityException e) {
            answerOfLOAD += ("----\nОшибка чтения файла.\n----");
            isWork = false;
        }
    }

    public String getAnswerOfLOAD() {
        return answerOfLOAD;
    }

    public void CSVtoLHS(String infoCSV) {
        CSVWriter csvWriter = new CSVWriter();
        plants.addAll( csvWriter.getArrPlants(infoCSV));
        if (plants.size() > 0)
            IsCSV = true;
        answerOfLOAD += ("----\nЭлементов было добавлено: " + plants.size() + "\n----");
        isWork = true;
    }

    /**
     * This method exit from program
     */
    protected String exit() {
        isWork = false;
        return ("Выход...");
    }

    public void saving() {
        System.out.println("----\nSaving...\n----");
        String newFile = "CSVObject" + new Integer((int) (Math.random() * 100)).toString();
        String directory = System.getProperty("user.dir");
        String separator = System.getProperty("file.separator");
        sourceFile = new File(directory + separator + newFile + ".csv");
        if (!sourceFile.exists() | !sourceFile.isFile()) {
            try {
                if (sourceFile.createNewFile()) {
                    answerOfSave += ("----\nНовый файл успешно создан.\nИмя файла:" + newFile + "\n----");
                    AskSave();
                }
            } catch (IOException e) {
                System.out.println("----\nОшибка при создании файла:\n----" + e.getMessage());
            }
        } else {
            AskSave();
        }
    }

    /**
     * This method save information about collection to file or if file missing, create new file
     */
    protected void saveAndExit() {
        if (sourceFile != null) {
            System.out.println("----\nSaving...\n----");
            String format = sourceFile.getName().substring(sourceFile.getName().indexOf(".") + 1);
            if (format.equals("csv"))
                save();
            else {
                if (!format.equals("csv") | sourceFile == null | !(sourceFile.canWrite())) {
                    answerOfSave += ("----\nФайл не существует или в него нельзя записать данные или он не соответствует необходимому для записи формату.\n----");
                    String newFile = "CSVObject" + new Integer((int) (Math.random() * 100)).toString();
                    String directory = System.getProperty("user.dir");
                    String separator = System.getProperty("file.separator");
                    sourceFile = new File(directory + separator + newFile + ".csv");
                    if (!sourceFile.exists() | !sourceFile.isFile()) {
                        try {
                            if (sourceFile.createNewFile()) {
                                answerOfSave += ("----\nНовый файл успешно создан.\nИмя файла:" + newFile + "\n----");
                                AskSave();
                            }
                        } catch (IOException e) {
                            System.out.println("----\nОшибка при создании файла:\n----" + e.getMessage());
                        }
                    } else {
                        AskSave();
                    }
                } else {
                    AskSave();
                }
            }
        }else
            saving();
    }

    protected void AskSave() {
        try {
            if (IsCSV)
                save();
            else {
                FileWriter fileWritercsv = new FileWriter(sourceFile);
                try (BufferedWriter bufferedWriter = new BufferedWriter(fileWritercsv)) {
                    bufferedWriter.write("");
                    answerOfSave += ("----\nКоллекция пуста\n---- ");
                } catch (IOException e) {
                    answerOfSave += ("----\nОшибка записи\n----");
                }
            }
        } catch (IOException e) {
            answerOfSave += ("----\nОшибка записи\n----" + e.getMessage());
        }
    }

    /**
     * This method save information about collection to file
     */
    protected void save() {
        try {
            BeforeSaveDelete(sourceFile);
            FileWriter fileWriterCSV = new FileWriter(sourceFile);
            try (BufferedWriter bufferedWriter = new BufferedWriter(fileWriterCSV)) {
                if (plants != null) {
                    if (IsCSV) {
                        CSVWriter csvWriter = new CSVWriter();
                        bufferedWriter.write(csvWriter.getWrittenPlants(plants));
                        answerOfSave += ("----\nКоллекция сохранена в файле: " + sourceFile.getAbsolutePath() + "\n----");
                    } else {
                        bufferedWriter.write(textOfFile);
                        answerOfSave += ("----\nСохранено изначальное значение файла\n----");
                    }
                } else {
                    bufferedWriter.write(textOfFile);
                }
            } catch (IOException e) {
                answerOfSave += ("----\nОшибка записи файла\n----" + e.getMessage());
            }
        } catch (IOException e) {
            answerOfSave += ("----\nОшибка записи файла\n----" + e.getMessage());
        }
    }

    public String getAnswerOfSave() {
        return answerOfSave;
    }

    /**
     * This method show information about this collection.
     */
    protected String info() {
        int count = 0;
        date = new Date();
        count = plants.stream().mapToInt((p) -> 1).sum();
        return ("----\n Информация о колекции:\n----\n" + "\t Тип: CopyOnWriteArraySet\n" + "\tСодержимое: экземпляры класса Plants\n" + "\tДата: " + date + "\n\tРазмер: " + count + "\n----");
    }

    /**
     * This method show elements in collection.
     */
    protected String show() {
        if (plants.size()>0) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("----\n[");
            plants.stream().forEach((p) -> stringBuilder.append(p.getName() + ','));
            stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
            return stringBuilder.append("]\n----").toString();
        }else
            return "[]";
    }

    /**
     * This method add new elements in collection.
     *
     * @param additionalPlants - Plants.class object, which you want to add.
     */
    protected String add(Plants additionalPlants) {
        plants.add(additionalPlants);
        IsCSV = true;
        return ("----\nЭлемент добавлен.\n----");
    }

    /**
     * This method add new elements in collection, if this element(object)
     * greater than the maximum element in the collection.
     *
     * @param additionalPlants - Plants.class object, which you want to add.
     */
    protected String add_if_max(Plants additionalPlants) {
        String answer = "";
        if (plants.size() > 0) {
        Iterator<Plants> iterator = plants.iterator();

        Comparator<Plants> comparator = Comparator.naturalOrder();
        Plants max = iterator.next();

            while (iterator.hasNext()) {
                if (comparator.compare(max, additionalPlants) == -1) {
                    max = iterator.next();
                }
                if (comparator.compare(max,additionalPlants) == 1){
                    plants.add(additionalPlants);
                    answer += ("----\nЭлемент добавлен\n----");
                } else
                    answer += ("----\nЭлемент не является максимальным.\n----");

            }
        }
        else {
            plants.add(additionalPlants);
            answer += "----\nКоллекция пуста. Элемент добавлен\n----";
        }
        return answer;
    }

    /**
     * This method remove all elements, which lower that element.
     *
     * @param removablePlants - Plants.class object, which will be compare with elements in collection.
     */
    protected String remove_lower(Plants removablePlants) {
        Iterator<Plants> iterator = plants.iterator();
        Comparator<Plants> comparator = Comparator.naturalOrder();
        int count = 0;
        if (plants.size() > 0) {
            while (iterator.hasNext()) {
                Plants element = iterator.next();
                if (comparator.compare(element, removablePlants) == -1) {
                    iterator.remove();
                    count++;
                }
            }
            return ("----\nКоличество удалённых элементов: " + count + "\n----");
        }else
            return ("----\nКоллекция пуста, удаление не возможно.\n----");
    }

    /**
     * This method remove all elements, which lower that element.
     *
     * @param removablePlants - Plants.class object, which will be compare with elements in collection.
     */
    protected String remove_greater(Plants removablePlants) {
        Iterator<Plants> iterator = plants.iterator();
        Comparator<Plants> comparator = Comparator.naturalOrder();
        if (plants.size() > 0) {
            int count = 0;
            while (iterator.hasNext()) {
                Plants element = iterator.next();
                if (comparator.compare(element, removablePlants) == 1) {
                    iterator.remove();
                    count++;
                }
            }
            return ("----\nКоличество удалённых элементов: " + count + "\n----");
        }else
            return ("----\nКоллекция пуста, удаление не возможно.\n----");
    }

    /**
     * This method remove element in collection equal to parametr.
     *
     * @param planted - Plants.class object,which will be compared with element in collection.
     */
    protected String remove(Plants planted) {
        Iterator<Plants> iterator = plants.iterator();
        Comparator<Plants> comparator = Comparator.naturalOrder();
        String answer = "";
        int HowMany = 0;
        boolean delete = false;
        if (plants.size() > 0) {
            while (iterator.hasNext()) {
                Plants element = iterator.next();
                if (comparator.compare(planted, element) == 0) {
                    iterator.remove();
                    delete = true;
                    HowMany++;
                }
            }
            if (HowMany == 1) {
                answer += ("----\nЭлемент успешно удалён.\n----");
            } else if (HowMany > 1) {
                answer += "----\nЭлементы успешно удалены.\n----";
            }
            if (!delete) {
                answer += ("----\nЭлемент не найден.\n----");
            }
            return answer;
        }
        else
            return ("----\nКоллекция пуста, удаление не возможно.\n----");
    }
}
