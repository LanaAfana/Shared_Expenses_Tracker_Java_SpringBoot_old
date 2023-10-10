package splitter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Group {
    private String name;
    private List<String> participants;

    public static boolean isAGroup(String name) {
        return name.matches("^[A-Z]+$");
    }

    public static boolean isValidOperation(String cmdStr, String cmd) {
        String[] cmdList = cmdStr.split("\\s+");
        if (cmdList[1].equals(cmd)
            && Group.isAGroup(cmdList[2])) {
            if (cmd.equals("create")
                    && cmdList.length > 3
//                    && cmdList[3].trim().substring(0, 0).equals('(')
//                    && cmdList[cmdList.length - 1].matches(".*\\)$")
            ) {
                return true;
            }
            return cmd.equals("show")
                    && cmdList.length == 3;
        }
        return false;
    }
}