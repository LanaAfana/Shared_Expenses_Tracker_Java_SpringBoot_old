package splitter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

public class Main {
    static List<Transaction> transactions = new ArrayList();
    static List<Group> groups = new ArrayList();
    static List<Transaction> result = new ArrayList();

    public static void main(String[] args) {

        while (true) {
            try (Scanner scanner = new Scanner(System.in)) {
                String cmd = scanner.nextLine().trim();
                if (cmd.equals("help")) {
                    System.out.println("balance\n" +
                            "borrow\n" +
                            "exit\n" +
                            "group\n" +
                            "help\n" +
                            "purchase\n" +
                            "repay");
                    continue;
                }
                if (cmd.equals("exit")) {
                    break;
                }
                String[] cmdList = cmd.trim().split("\\s+");
                if (cmd.contains("group ")) {
                    if (cmd.contains("create")) {
                        createGroup(cmd.trim());
                        continue;
                    } else if (cmd.contains("show")) {
                        showGroup(cmd.trim());
                        continue;
                    }
                }
                if (cmd.contains("purchase")) {
                    doPurchase(cmdList);
                    continue;
                }
                if (cmd.contains("balance")) {
                    doBalance(cmdList);
                    continue;
                }
                if (cmd.contains("borrow ")) {
                    doTransaction(cmdList, "borrow");
                    continue;
                }
                if (cmd.contains("repay ")) {
                    doTransaction(cmdList, "repay");
                    continue;
                }
                System.out.println("Unknown command. Print help to show commands list");
            }
        }
    }

    private static void doPurchase(String[] cmdList) {
        if (Transaction.isValidOperation(cmdList, "purchase")) {
            String groupName = cmdList[cmdList.length - 1]
                    .substring(1, cmdList[cmdList.length - 1].length() - 1);
            if (Group.isAGroup(groupName)) {
                int shift = cmdList.length == 5 ? 0 : 1;
                LocalDate date = cmdList[0].matches("purchase") ?
                        LocalDate.now() :
                        LocalDate.parse(cmdList[0].replace('.', '-'));
                String acc_credit = cmdList[shift + 1].trim();
                Group group = groups.stream()
                        .filter(x -> x.getName().equals(groupName))
                        .findFirst()
                        .get();

                Double sum = Double.parseDouble(cmdList[shift + 3].trim())
                        / group.getParticipants().size();
                System.out.println(sum);
                for (int i = 0; i < group.getParticipants().size(); i++) {
                    String participant = group.getParticipants().get(i);
                    if (participant.equals(acc_credit)) {
                        continue;
                    }
                    if (i == group.getParticipants().size() - 1) {
                        sum = Double.parseDouble(cmdList[shift + 3].trim()) - new BigDecimal(sum)
                                .setScale(2, RoundingMode.HALF_UP)
                                .doubleValue() * (group.getParticipants().size() - 1);
                        System.out.println(sum);
                    } else {
                        sum = new BigDecimal(sum)
                                .setScale(2, RoundingMode.HALF_UP)
                                .doubleValue();
                        System.out.println(sum);
                    }
                    transactions.add(new Transaction(date,
                            participant,
                            acc_credit,
                            sum));
                }
            } else {
                System.out.println("Unknown group");
            }
        } else {
            printCmdError();
        }
    }

    private static void showGroup(String cmdStr) {
        if (Group.isValidOperation(cmdStr, "show")) {
            String name = cmdStr.split("\\s+")[2];
            if (groups.stream()
                    .filter(x -> x.getName().equals(name))
                    .findFirst().isPresent()) {
                groups.stream()
                        .filter(x -> x.getName().equals(name))
                        .findFirst()
                        .get()
                        .getParticipants()
                        .stream().sorted()
                        .forEach(System.out::println);
            } else {
                System.out.println("Unknown group");
            }
        } else {
            printCmdError();
        }
    }

    private static void createGroup(String cmdStr) {
        if (Group.isValidOperation(cmdStr, "create")) {
            String[] cmdList = cmdStr.split("\\(");
            String name = cmdList[0].trim().split("\\s+")[2].trim();
            List<String> listParticipants = Arrays.stream(cmdList[1]
                    .replace(")", "")
                    .split(",\\s*"))
                    .sorted()
                    .toList();
            groups.add(new Group(name, listParticipants));
        } else {
            printCmdError();
        }
    }

    private static void doTransaction(String[] cmdList, String cmd) {
        if (Transaction.isValidOperation(cmdList, "borrow")
        || Transaction.isValidOperation(cmdList, "repay")) {
            int shift = cmdList.length == 4 ? 0 : 1;

            LocalDate date = cmdList[0].matches("borrow|repay") ?
                    LocalDate.now() :
                    LocalDate.parse(cmdList[0].replace('.', '-'));
            double mult = cmd.equals("borrow") ? 1.00 : -1.00;
            Double sum = Double.parseDouble(cmdList[shift + 3]);
            sum = cmd.equals("borrow")
                    ? sum
                    : new BigDecimal(-1.00 * sum)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();

            transactions.add(new Transaction(date,
                    cmdList[shift + 1],
                    cmdList[shift + 2],
                    sum));
        } else {
            printCmdError();
        }
    }

    private static void doBalance(String[] cmdList) {

        if (Transaction.isValidBalance(cmdList)) {
            LocalDate date;
            if (cmdList[1].equals("balance")) {
                date = LocalDate.parse(cmdList[0].replace('.', '-'));
                if (cmdList.length == 3 && cmdList[2].equals("open"))
                    date = date.withDayOfMonth(1).minusDays(1);
            } else {
                date = LocalDate.now();
                if (cmdList.length == 2 && cmdList[1].equals("open")) {
                    date = date.withDayOfMonth(1).minusDays(1);
                }
            }

            result.clear();
            for (Transaction trsc : transactions) {
                if (trsc.getDate().isAfter(date)) {
                    continue;
                }
                if (result.isEmpty()) {
                    result.add(new Transaction(trsc.getDate(), trsc.getAcc_debit(), trsc.getAcc_credit(), trsc.getSum()));
                    continue;
                }
                ListIterator<Transaction> iterator = result.listIterator();
                boolean isExistsRecord = false;
                do {
                        Transaction curTransaction = iterator.next();
                        if (curTransaction.getAcc_debit().equals(trsc.getAcc_debit())
                                && curTransaction.getAcc_credit().equals(trsc.getAcc_credit())) {
                            curTransaction.setSum(curTransaction.getSum() + trsc.getSum());
                            isExistsRecord = true;
                        } else if (curTransaction.getAcc_debit().equals(trsc.getAcc_credit())
                                && curTransaction.getAcc_credit().equals(trsc.getAcc_debit())) {
                            curTransaction.setSum(curTransaction.getSum() - trsc.getSum());
                            isExistsRecord = true;
                        }
                } while (iterator.hasNext());

                if (isExistsRecord == false) {
                    iterator.add(new Transaction(trsc.getDate(), trsc.getAcc_debit(), trsc.getAcc_credit(), trsc.getSum()));
                }
            }
            outputBalance();
        } else {
            printCmdError();
        }
    }

    private static void outputBalance() {
        boolean hasRepayments = false;
        List<Transaction> resList = result.stream()
                .sorted(Comparator.comparing(x -> x.getAcc_credit()))
                .sorted(Comparator.comparing(x -> x.getAcc_debit()))
                .toList();
        for (Transaction rec : resList) {
            Double sum = new BigDecimal(rec.getSum())
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
            if (sum > 0.00) {
                hasRepayments = true;
                System.out.printf("%s owes %s %.2f%n",
                        rec.getAcc_debit(),
                        rec.getAcc_credit(),
                        sum);
            } else if (sum < 0.00){
                hasRepayments = true;
                System.out.printf("%s owes %s %.2f%n",
                        rec.getAcc_credit(),
                        rec.getAcc_debit(),
                        -1 * sum);
            }
        }
        if (!hasRepayments)
            System.out.println("No repayments");
    }

    private static void printCmdError() {
        System.out.println("Illegal command arguments");
    }
}
