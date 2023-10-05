package splitter;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Transaction {
    private LocalDate date;
    private String acc_debit;
    private String acc_credit;
    private Integer sum;

    static boolean isValidOperation(String[] cmdList, String command) {
        if (cmdList.length < 4
                || (cmdList.length == 4 && !cmdList[0].equals(command))
                || (cmdList.length == 5 && !cmdList[1].equals(command))) {
            return false;
        }
        return true;
    }


    static boolean isValidBalance(String[] cmdList) {
        if ((cmdList.length == 1 && !cmdList[0].equals("balance"))
                || (cmdList.length == 2 && !(cmdList[0].equals("balance") || cmdList[1].equals("balance")))
                || (cmdList.length == 3 && (!cmdList[1].equals("balance") || !cmdList[2].matches("open|close")))) {
            return false;
        }
        return true;
    }
}
