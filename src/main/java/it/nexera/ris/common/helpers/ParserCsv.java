package it.nexera.ris.common.helpers;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Vladimir Zrazhevskiy
 * InfoStroy Co., 2012.
 */

public class ParserCsv {

    private File file;

    private List<List<String>> cells;

    public ParserCsv(File file) {
        super();
        this.file = file;
    }

    public void parse() throws IOException {

        cells = new ArrayList<>();

        try (FileInputStream fStream = new FileInputStream(file)) {
            try (DataInputStream in = new DataInputStream(fStream)) {
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine = null;
                while ((strLine = br.readLine()) != null) {
                    cells.add(parseLine(strLine));
                }
            }
        }
    }

    private List<String> parseLine(String line) {
        List<String> row = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(line, ",");
        while (tokenizer.hasMoreTokens()) {
            row.add(tokenizer.nextToken());
        }

        return row;
    }

    public String getCell(int row, int column) {
        return cells.get(row).get(column);
    }

    public List<String> getRow(int i) {
        return cells.get(i);
    }

    public int getRowLength(int i) {
        return cells.get(i).size();
    }

    public int getRowsLength() {
        return cells.size();
    }
}
