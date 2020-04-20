import java.awt.*;
import javax.swing.*;

import java.util.*;
import java.awt.event.*;
import javax.swing.event.*;

import java.awt.Toolkit;


public class JBrainTetris extends JTetris{
    protected DefaultBrain brain;


    //controls
    static protected JCheckBox brainMode;
    protected JSlider adversary;
    protected JLabel advStatus;

    protected Random randomizer;



    protected int prevCount;
    Brain.Move bestmove;

    JBrainTetris(int pixels){
        super(pixels);
        brain = new DefaultBrain();
        prevCount = -1;
        randomizer = new Random();
    }


    @Override
    public JComponent createControlPanel() {
        JPanel panel = (JPanel)super.createControlPanel();

        //add brain checkbox
        panel.add(new JLabel("Brain:"));
        brainMode = new JCheckBox("Brain active");
        brainMode.setSelected(false);
        panel.add(brainMode);

        JPanel brainRow = new JPanel();

        // adversary slider
        panel.add(Box.createVerticalStrut(12));
        brainRow.add(new JLabel("Adversary:"));
        adversary = new JSlider(0, 100, 0);	// min, max, current
        adversary.setPreferredSize(new Dimension(100, 15));

        brainRow.add(adversary);
        panel.add(brainRow);

        panel.add(Box.createVerticalStrut(12));
        advStatus = new JLabel();
        panel.add(advStatus);

        return panel;
    }

    @Override
    public void stopGame() {
        advStatus.setText("");
        super.stopGame();
    }

    @Override
    public Piece pickNextPiece(){
        int randVal = randomizer.nextInt(98) + 1;
        int worstIndex = 0;
        double worstScore = Double.MIN_VALUE;
        double currScore;

        if (randVal > adversary.getValue()){
            advStatus.setText("ok");
            return super.pickNextPiece();
        }

        advStatus.setText("*ok*");
        for (int i = 0; i< pieces.length; i++){
            board.undo();
            currScore  = brain.bestMove(board, pieces[i],HEIGHT, null).score;
            if(currScore > worstScore)
            {
                worstIndex  = i;
                worstScore = currScore;
            }
        }
        return pieces[worstIndex];
    }

    @Override
    public void tick(int verb) {
        if (verb != DOWN) {
            super.tick(verb);
            return;
        }
        if (brainMode.isSelected()) {
                if (count != prevCount) {
                    //remove piece from position for bestmove
                    board.undo();
                    bestmove = brain.bestMove(board, currentPiece, HEIGHT, bestmove);
                    prevCount = count;
                }
                if (bestmove == null)
                    return;
                //if rotation is not best, we can rotate once
                if (!bestmove.piece.equals(currentPiece)) {
                    super.tick(ROTATE);
                }
                //head to the direction of best placement or drop if best state is achieved
                if (bestmove.x < currentX)
                    super.tick(LEFT);
                else if (bestmove.x > currentX)
                    super.tick(RIGHT);
                else if (bestmove.x == currentX && bestmove.piece.equals(currentPiece)) {
                    //we need to call tick(down) twice, to make placement final
                    super.tick(DROP);
                    super.tick(DOWN);
                }
        }
        super.tick(DOWN);
    }

    public static void main(String[] args) {
        // Set GUI Look And Feel Boilerplate.
        // Do this incantation at the start of main() to tell Swing
        // to use the GUI LookAndFeel of the native platform. It's ok
        // to ignore the exception.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        JBrainTetris tetris = new JBrainTetris(16);
        JFrame frame = JBrainTetris.createFrame(tetris);
        frame.setVisible(true);
    }
}
