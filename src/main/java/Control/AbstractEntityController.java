package Control;

import ADT.ListInterface;
import ADT.ArrayList;
import Utility.ControllerResult;
import Utility.FileUtility;

public abstract class AbstractEntityController<T, K> {
    protected ListInterface<T> list;
    protected final String filePath;

    public AbstractEntityController(String filePath) {
        this.filePath = filePath;
        this.list = new ArrayList<>();
    }

    protected abstract T parseCsvLine(String line);

    protected abstract String toCsvLine(T item);

    protected abstract K getKey(T item);

    protected void loadFromFile() {
        String[] lines = FileUtility.readLines(filePath);
        for (String line : lines) {
            list.add(parseCsvLine(line));
        }
    }

    protected void saveToFile() {
        String[] lines = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            lines[i] = toCsvLine(list.getEntry(i + 1));
        }
        FileUtility.writeAllLines(filePath, lines);
    }

    public ControllerResult add(T item) {
        if (findByKey(getKey(item)) != null) {
            return ControllerResult.fail("Item already exists: " + getKey(item));
        }
        list.add(item);
        saveToFile();
        return ControllerResult.success();
    }

    public ControllerResult delete(K key) {
        int indexToDelete = findIndexByKey(key);
        if (indexToDelete == -1) {
            return ControllerResult.fail("Item not found: " + key);
        }
        list.remove(indexToDelete);
        saveToFile();
        return ControllerResult.success();
    }

    public T findByKey(K key) {
        int index = findIndexByKey(key);
        if (index == -1) {
            return null;
        }
        return list.getEntry(index);
    }

    protected int findIndexByKey(K key) {
        for (int i = 0; i < list.size(); i++) {
            if (getKey(list.getEntry(i + 1)).equals(key)) {
                return i + 1;
            }
        }
        return -1;
    }

    public ListInterface<T> getAll() {
        return list;
    }
}
