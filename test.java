public class test {
    public static void main(String args[]) {
        String[][] board = {
            {"X", "O", "X"},
            {"X", "O", "O"},
            {"X", "*", "*"}
        };
        int size = board.length;
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < size; i++) {
            sb.append("|");
            
            for (int j = 0; j < size; j++) {
                sb.append(board[i][j]);
                if (j < size - 1) {
                    sb.append("|");
                }
            }
        }
        sb.append("|");
        System.out.println(sb.toString());
    }
}
