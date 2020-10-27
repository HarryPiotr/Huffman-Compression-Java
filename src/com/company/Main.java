package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

public class Main {

    public static void main(String[] args) {

        String mode = args[0].toLowerCase();
        args = Arrays.copyOfRange(args, 1, args.length);

        switch(mode) {
            case "code":
                for (String a : args) {
                    File inputFile = new File(a);
                    System.out.println("-----------------------------");
                    System.out.println(a + " - sprawdzam czy plik istnieje");
                    if (!checkIfFileExists(inputFile)) continue;
                    System.out.println(a + " - zliczam symbole wejsciowe");
                    ArrayList<Node> nodes = countSymbols(inputFile);
                    System.out.println(a + " - sortuje model zrodla danych");
                    sortNodeList(nodes);
                    System.out.println(a + " - buduje drzewo");
                    ReplacementNode treeRoot = buildTree(nodes);
                    System.out.println(a + " - tworze slowa kodowe");
                    createCodingSequences(treeRoot, "");
                    System.out.println(a + " - kompresuje plik");
                    codeFile(nodes, a, inputFile);
                    System.out.println(a + " - zapisuje tabele kodowa");
                    saveCodingTable(nodes, a);
                }
                break;
            case "decode":
                for (String a : args) {
                    System.out.println("-----------------------------");
                    if(a.contains(".huff")) a = a.substring(0, a.length() - 5);
                    File codedFile = new File(a + ".huff");
                    File codingFile = new File(a + ".coding");
                    File decodedFile = new File(a + ".dehuff");
                    System.out.println(a + " - sprawdzam czy istnieje skompresowany plik");
                    if (!checkIfFileExists(codedFile)) continue;
                    System.out.println(a + " - sprawdzam czy istnieje tabela kodowa");
                    if (!checkIfFileExists(codingFile)) continue;
                    System.out.println(a + " - odbudowuje drzewo kodowania");
                    ReplacementNode codingRoot = rebuildTree(codingFile);
                    System.out.println(a + " - dekoduje plik");
                    decodeFile(codedFile, decodedFile, codingRoot);
                }
                break;
            default:
                System.out.println("Nie ma takiej funkcji.");
                System.out.println("code [arg1] [arg2] ... - skompresuj pliki");
                System.out.println("decode [arg1] [arg2] ... - dekompresuj pliki");
        }
    }

    private static boolean checkIfFileExists(File f) {
        try {
            InputStream is = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            System.out.println(f.getName() + ": Nie znaleziono pliku.");
            return false;
        }
        return true;
    }

    private static ArrayList<Node> countSymbols(File f) {
        //Tutaj zaimplementować czytanie pliku bajt po bajcie i zapisywanie informacji o liczbie wystąpień do listy

        ArrayList<Node> nodes = new ArrayList<>();

        try {
            InputStream is = new FileInputStream(f);
            byte[] b = {0};
            while (is.read(b) != -1) {
                Node n = findNode(nodes, b[0]);
                if (n == null) {
                    Node newNode = new Node(b[0]);
                    nodes.add(newNode);
                } else n.incrementOccurrences();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(f.getName() + ": Niespodziewany blad podczas czytania pliku.");
        }
        return nodes;
    }

    private static void sortNodeList(ArrayList<Node> nodes) {
        //Tutaj zaimplementować dowolny algorytm sortowania listy pod względem ilości wystąpień
        nodes.sort(Node.NodeOccurancesComparator);
    }

    private static ReplacementNode buildTree(ArrayList<Node> nodes) {
        //Tutaj zaimplementować budowanie drzewa Huffmana z obiektów typu Node i ReplacementNode
        int numberOfReplacements = 0;
        ArrayList<Node> nodesCopy = new ArrayList<>(nodes);
        while (nodesCopy.size() != 1) {
            Node n1 = nodesCopy.get(0);
            Node n2 = nodesCopy.get(1);
            ReplacementNode r = new ReplacementNode(n1, n2, (byte) (++numberOfReplacements * -1));
            nodesCopy.remove(n1);
            nodesCopy.remove(n2);
            nodesCopy.add(r);
            nodesCopy.sort(Node.NodeOccurancesComparator);
        }
        return (ReplacementNode) nodesCopy.get(0);
    }

    private static void createCodingSequences(Node node, String prefix) {
        if (!(node instanceof ReplacementNode)) node.setCodingSequence(prefix);
        else {
            ReplacementNode rnode = (ReplacementNode) node;
            createCodingSequences(rnode.getLeft(), prefix + "0");
            createCodingSequences(rnode.getRight(), prefix + "1");
        }
    }

    private static void codeFile(ArrayList<Node> nodes, String filename, File in) {
        File outputFile = new File(filename + ".huff");
        ArrayList<Node> flippedNodes = new ArrayList<>(nodes);
        flippedNodes.sort(Node.NodeOccurancesComparatorDescending);

        try {
            InputStream is = new FileInputStream(in);
            OutputStream os = new FileOutputStream(outputFile);
            byte[] b = {0};
            StringBuffer buffer = new StringBuffer();
            while (is.read(b) != -1) {
                Node n = findNode(nodes, b[0]);
                buffer.append(n.getCodingSequence());
                if (buffer.length() >= 8) {
                    String seq = buffer.substring(0, 8);
                    buffer.delete(0, 8);
                    byte nb = (byte) Integer.parseInt(seq, 2);
                    os.write(nb);
                }
            }
            if (buffer.length() > 0) {
                while (buffer.length() < 8) buffer.append("0");
                String seq = buffer.substring(0, 8);
                byte nb = (byte) Integer.parseInt(seq, 2);
                os.write(nb);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(in.getName() + ": Niespodziewany blad podczas czytania pliku.");
        }
    }

    private static void saveCodingTable(ArrayList<Node> nodes, String filename) {
        try {
            PrintWriter of = new PrintWriter(filename + ".coding");
            for(Node n : nodes) {
                of.println(n.getSymbol() + ":" + n.getCodingSequence() + ":" + n.getOccurrences());
            }
            of.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static ReplacementNode rebuildTree(File codingFile) {

        ArrayList<Node> codingNodes = new ArrayList<>();

        try {
            FileReader fr = new FileReader(codingFile);
            BufferedReader br = new BufferedReader(fr);
            String fileLine;
            do {
                fileLine = br.readLine();
                if(fileLine != null) {
                    StringTokenizer st = new StringTokenizer(fileLine, ":");
                    byte b = Byte.parseByte(st.nextToken());
                    String seq = st.nextToken();
                    long oc = Long.parseLong(st.nextToken());
                    Node n = new Node(b);
                    n.setOccurrences(oc);
                    n.setCodingSequence(seq);
                    codingNodes.add(n);
                }
            } while(fileLine != null);
        } catch (FileNotFoundException e) {
            System.out.println(codingFile.getName() + ": Nie znaleziono tabeli kodowej");
        } catch (IOException e) {
            e.printStackTrace();
        }

        sortNodeList(codingNodes);
        return buildTree(codingNodes);
    }

    private static void decodeFile(File inputFile, File outputFile, ReplacementNode root) {

        try {
            InputStream is = new FileInputStream(inputFile);
            OutputStream os = new FileOutputStream(outputFile);

            byte[] b = {0};
            StringBuffer buffer = new StringBuffer();
            Node n = root;
            while (is.read(b) != -1) {
                int bi = b[0] & 0xFF;   //Poniewaz byte przyjmuje wartosc od -128 do 127
                for (int i = 0; i < 8; i++) {
                    buffer.insert(0, "" + bi % 2);
                    bi /= 2;
                }

                while (buffer.length() > 0) {
                    if (buffer.substring(0, 1).equals("0")) n = ((ReplacementNode) n).getLeft();
                    else n = ((ReplacementNode) n).getRight();
                    buffer.deleteCharAt(0);
                    if (!(n instanceof ReplacementNode)) {
                        os.write(n.getSymbol());
                        n = root;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(inputFile.getName() + ": Niespodziewany blad podczas czytania pliku.");
        }
    }

    private static Node findNode(ArrayList<Node> nodes, byte b) {
        for (Node n : nodes) {
            if (n.getSymbol() == b) return n;
        }
        return null;
    }
}
