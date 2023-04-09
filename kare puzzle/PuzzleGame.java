import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

public class PuzzleGame {

    private JFrame frame;
    private JPanel mainPanel;
    private JButton[][] puzzleButtons;
    private BufferedImage originalImage;
    private BufferedImage[][] puzzleImages;
    private LinkedList<PuzzlePiece> puzzlePieces = new LinkedList<>();
    private int score;
    private int moveCount;
    private JLabel scoreLabel;
    private JLabel moveCountLabel;
    private static final String HIGH_SCORE_FILE = "enyuksekskor.txt";
    private ArrayList<HighScoreEntry> highScores;
    private PuzzlePiece selectedPiece = null;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                PuzzleGame game = new PuzzleGame();
                game.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public PuzzleGame() {
        init();
        this.highScores = new ArrayList<>();
    }

    private void init() {
        createUI();
    }

    private static class PuzzlePiece {
        int position;
        BufferedImage image;

        public PuzzlePiece(int position, BufferedImage image) {
            this.position = position;
            this.image = image;
        }
    }

    

    private void createUI() {
        frame = new JFrame();
        frame.setTitle("Kare Puzzle");
        frame.setBounds(100, 100, 600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(0, 0));
        frame.setResizable(false);

        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(4, 4));
        frame.getContentPane().add(mainPanel, BorderLayout.CENTER);

        JButton restartButton = new JButton("Yeniden Başlat");
        frame.getContentPane().add(restartButton, BorderLayout.NORTH);
        restartButton.addActionListener(e -> {
            frame.dispose();
            PuzzleGame game = new PuzzleGame();
            game.frame.setVisible(true);
        });

        puzzleButtons = new JButton[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                puzzleButtons[i][j] = new JButton("P" + (i * 4 + j));
                puzzleButtons[i][j].setText("");
                mainPanel.add(puzzleButtons[i][j]);
            }
        }
        JButton loadImageButton = new JButton("Resim Yükle");
        loadImageButton.addActionListener(e -> loadImage());
        frame.getContentPane().add(loadImageButton, BorderLayout.WEST);

       
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int position = i * 4 + j;
                puzzleButtons[i][j].addActionListener(e -> swapPieces(position));
            }
        }

        JButton shuffleButton = new JButton("Karıştır");
        shuffleButton.addActionListener(this::shufflePuzzle);
        frame.getContentPane().add(shuffleButton, BorderLayout.SOUTH);

        JPanel statusPanel = new JPanel();
        scoreLabel = new JLabel("Skor: 0");
        moveCountLabel = new JLabel("Hamle Sayısı: 0");
        statusPanel.add(scoreLabel);
        statusPanel.add(moveCountLabel);
        frame.getContentPane().add(statusPanel, BorderLayout.NORTH);

        JButton highScoresButton = new JButton("Yüksek Skorlar");
        highScoresButton.addActionListener(e -> showHighScores());
        frame.getContentPane().add(highScoresButton, BorderLayout.EAST);
        int gap = 2;
        mainPanel.setLayout(new GridLayout(4, 4, gap, gap));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(shuffleButton);
        buttonPanel.add(restartButton);
        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void shufflePuzzle(ActionEvent e) {
        if (puzzlePieces != null) {
            Collections.shuffle(puzzlePieces);
            updatePuzzleButtons();
            checkPuzzleCompletion();
        }
    }

    private boolean checkPuzzleCompletion() {
        boolean completed = true;
        for (int i = 0; i < puzzlePieces.size(); i++) {
            PuzzlePiece piece = puzzlePieces.get(i);
            if (piece.position != i) {
                completed = false;
                break;
            }
        }

        if (completed) {
            JOptionPane.showMessageDialog(frame, "Tebrikler! Puzzle'ı tamamladınız!");
        }
        return completed;
    }

    private void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(frame);
    
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                originalImage = ImageIO.read(selectedFile);
                puzzleImages = splitImage(originalImage, 4, 4);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    
        puzzlePieces = new LinkedList<>();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int position = i * 4 + j;
                puzzlePieces.add(new PuzzlePiece(position, puzzleImages[i][j]));
            }
        }
    
        updatePuzzleButtons(); 
    }

    private BufferedImage[][] splitImage(BufferedImage image, int rows, int cols) {
        int imgWidth = image.getWidth() / cols;
        int imgHeight = image.getHeight() / rows;
        BufferedImage[][] images = new BufferedImage[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                images[i][j] = image.getSubimage(j * imgWidth, i * imgHeight, imgWidth, imgHeight);
            }
        }

        return images;
    }

    private void swapPieces(int clickedPosition) {
        PuzzlePiece clickedPiece = puzzlePieces.get(clickedPosition);

        if (clickedPiece.image == null) {
        
            return;
        }

        if (selectedPiece == null) {
           
            selectedPiece = clickedPiece;
        } else {
            boolean selectedCorrectBefore = selectedPiece.position == puzzlePieces.indexOf(selectedPiece);
            boolean clickedCorrectBefore = clickedPiece.position == clickedPosition;

            boolean selectedPieceCorrect = selectedPiece.position == puzzlePieces.indexOf(selectedPiece);
            boolean clickedPieceCorrect = clickedPiece.position == clickedPosition;

          
            if (selectedPieceCorrect || clickedPieceCorrect) {
                selectedPiece = null;
                return;
            }

            int tempPosition = selectedPiece.position;
            BufferedImage tempImage = selectedPiece.image;

            selectedPiece.position = clickedPiece.position;
            selectedPiece.image = clickedPiece.image;

            clickedPiece.position = tempPosition;
            clickedPiece.image = tempImage;

            selectedPiece = null; 

            moveCount++;
            moveCountLabel.setText("Hamle Sayısı: " + moveCount);

            boolean selectedCorrectAfter = tempPosition == puzzlePieces.indexOf(clickedPiece);
            boolean clickedCorrectAfter = clickedPiece.position == clickedPosition;

            if (selectedCorrectBefore != selectedCorrectAfter) {
                score += selectedCorrectAfter ? 5 : -10;
            }

            if (clickedCorrectBefore != clickedCorrectAfter) {
                score += clickedCorrectAfter ? 5 : -10;
            }

            
            if (selectedCorrectAfter && clickedCorrectAfter) {
                score -= 5;
            }
            if (checkPuzzleCompletion()) {
                
                score += 5;
                scoreLabel.setText("Skor: " + score);
                JOptionPane.showMessageDialog(frame, "Tebrikler! Puzzle'ı tamamladınız!");
                saveHighScore();
            }
            scoreLabel.setText("Skor: " + score);

            updatePuzzleButtons();
        }
    }

    private void updatePuzzleButtons() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int position = i * 4 + j;
                PuzzlePiece piece = puzzlePieces.get(position);
                if (piece.image != null) {
                    ImageIcon imageIcon = new ImageIcon(piece.image);
                    puzzleButtons[i][j].setIcon(imageIcon);
                } else {
                    puzzleButtons[i][j].setIcon(null);
                }

               
                puzzleButtons[i][j].setPreferredSize(new Dimension(piece.image.getWidth(), piece.image.getHeight()));
            }
        }
       
        frame.pack();

    }

    private static class HighScoreEntry {
        String playerName;
        int moveCount;
        int score;

        public HighScoreEntry(String playerName, int moveCount, int score) {
            this.playerName = playerName;
            this.moveCount = moveCount;
            this.score = score;
        }

        public int getScore() {
            return score;
        }
    }

    

    private void saveHighScore() {
        String playerName = JOptionPane.showInputDialog(frame, "Adınızı girin:");
        if (playerName == null || playerName.trim().isEmpty()) {
            return;
        }

        highScores.add(new HighScoreEntry(playerName, moveCount, score));
        highScores.sort(Comparator.comparingInt(HighScoreEntry::getScore).reversed());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            for (HighScoreEntry entry : highScores) {
                writer.write(String.format("%s, %d, %d%n", entry.playerName, entry.moveCount, entry.score));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showHighScores() {
        StringBuilder message = new StringBuilder("En Yüksek Skorlar:\n\n");
        for (int i = 0; i < highScores.size() && i < 10; i++) {
            HighScoreEntry entry = highScores.get(i);
            message.append(String.format("%d. %s - %d puan - %d hamle%n", i + 1, entry.playerName, entry.score,
                    entry.moveCount));
        }
        JOptionPane.showMessageDialog(frame, message.toString());
    }

    private boolean isPuzzleShuffled() {
        for (int i = 0; i < puzzlePieces.size(); i++) {
            PuzzlePiece piece = puzzlePieces.get(i);
            if (piece.position != i) {
                return true;
            }
        }
        return false;

    }

}
