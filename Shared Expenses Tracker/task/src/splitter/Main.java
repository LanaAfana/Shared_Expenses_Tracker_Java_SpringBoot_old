package splitter;

import java.time.LocalDate;
import java.util.*;

public class Main {
    static List<Transaction> transactions = new ArrayList();
    static List<Transaction> result = new ArrayList();

    public static void main(String[] args) {

        while (true) {
            try (Scanner scanner = new Scanner(System.in)) {
                String cmd = scanner.nextLine().trim();
                if (cmd.equals("help")) {
                    System.out.println("balance\n" +
                            "borrow\n" +
                            "exit\n" +
                            "help\n" +
                            "repay");
                    continue;
                }
                if (cmd.equals("exit")) {
                    break;
                }
                String[] cmdList = cmd.trim().split(" ");
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

    private static void doTransaction(String[] cmdList, String cmd) {
        if (Transaction.isValidOperation(cmdList, "borrow")
        || Transaction.isValidOperation(cmdList, "repay")) {
            int shift = cmdList.length == 4 ? 0 : 1;

            LocalDate date = cmdList[0].matches("borrow|repay") ?
                    LocalDate.now() :
                    LocalDate.parse(cmdList[0].replace('.', '-'));
            int mult = cmd.equals("borrow") ? 1 : -1;

            transactions.add(new Transaction(date,
                    cmdList[shift + 1],
                    cmdList[shift + 2],
                    Integer.parseInt(cmdList[shift + 3]) * mult));
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
                .sorted(Comparator.comparing(x -> x.getAcc_debit()))
                .toList();
        for (Transaction rec : resList) {
            if (rec.getSum() > 0) {
                hasRepayments = true;
                System.out.printf("%s owes %s %d%n",
                        rec.getAcc_debit(),
                        rec.getAcc_credit(),
                        rec.getSum());
            } else if (rec.getSum() < 0){
                hasRepayments = true;
                System.out.printf("%s owes %s %d%n",
                        rec.getAcc_credit(),
                        rec.getAcc_debit(),
                        -1 * rec.getSum());
            }
        }
        if (!hasRepayments)
            System.out.println("No repayments");
    }

    private static void printCmdError() {
        System.out.println("Illegal command arguments");
    }
}
