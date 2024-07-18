package com.sudoku.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.sudoku.model.SudokuSolver;

@WebServlet("/game")
public class GameServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        int userId = (int) session.getAttribute("userId");

        // Retrieve the current game state from the request
        int[][] sudoku = new int[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                String cellValue = request.getParameter("cell_" + i + "_" + j);
                if (cellValue == null || cellValue.isEmpty()) {
                    sudoku[i][j] = 0; // Empty cell
                } else {
                    try {
                        sudoku[i][j] = Integer.parseInt(cellValue);
                    } catch (NumberFormatException e) {
                        sudoku[i][j] = 0; // Invalid input, treat as empty cell
                    }
                }
            }
        }

        // Print initial Sudoku state for debugging
        System.out.println("Initial Sudoku State:");
        for (int[] row : sudoku) {
            System.out.println(Arrays.toString(row));
        }

        // Solve the Sudoku puzzle
        SudokuSolver solver = new SudokuSolver();
        if (solver.solve(sudoku)) {
            // Print solved Sudoku state for debugging
            System.out.println("Solved Sudoku State:");
            for (int[] row : sudoku) {
                System.out.println(Arrays.toString(row));
            }
        } else {
            System.out.println("No solution exists for the provided Sudoku puzzle.");
        }

        // Save the solved game state in the database
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/SudokuGame", "root", "password");
            PreparedStatement pst = conn.prepareStatement("INSERT INTO sudoku_games(user_id, game_state) VALUES(?, ?)");
            pst.setInt(1, userId);
            pst.setString(2, Arrays.deepToString(sudoku));
            pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Redirect to the game.jsp page with the solved Sudoku
        request.setAttribute("sudoku", sudoku);
        request.getRequestDispatcher("game.jsp").forward(request, response);
    }
}
