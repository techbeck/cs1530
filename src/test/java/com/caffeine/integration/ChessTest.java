// First-Party Imports
import javax.swing.*;
import java.awt.Frame;
import java.awt.Container;
import java.awt.Component;
import java.awt.Color;
import java.awt.event.*;
import java.io.File;

// Third-Party Imports
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.assertj.swing.core.*;
import org.assertj.swing.fixture.*;
import org.assertj.swing.exception.*;
import org.assertj.swing.core.matcher.*;
import org.assertj.swing.core.matcher.JButtonMatcher.*;
import org.assertj.swing.exception.WaitTimedOutError;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.swing.launcher.ApplicationLauncher.application;
import static org.assertj.swing.finder.WindowFinder.findFrame;
import static org.assertj.swing.finder.JOptionPaneFinder.findOptionPane;
import static org.assertj.swing.finder.JFileChooserFinder.findFileChooser;
import static org.assertj.swing.fixture.Containers.showInFrame;

// Local Imports
import com.caffeine.Chess;
import com.caffeine.view.*;


/* ============================================================================
                                Integration Tests
============================================================================ */
public class ChessTest {

    @Test // Sanity Check -- Is Chess properly imported and accessible?
    public void testChessIntegrated(){
        Chess game = new Chess();
    }

    static FrameFixture frame;
    static Robot robot;

    @BeforeClass
    public static void setUp() {
        application(Chess.class).start();
        robot = BasicRobot.robotWithCurrentAwtHierarchy();
        frame = findFrame("frame").using(robot);
    }

    @AfterClass
    public static void tearDown(){
        frame.close();
        frame.cleanUp();
    }

    @Test
    public void testWindowIsNotResizeable(){
        boolean threwError = false;

        try{
            frame.resizeWidthTo(250);
        }
        catch(IllegalStateException e){
            threwError = true;
        }

        assertTrue("Window Should not be Resizeable", threwError);
    }

    @Test
    public void testLoadButtonCreatesOptionPane(){
        frame.button("loadButton").click();
        try {
            // in case unsaved current game
            JOptionPaneFixture optionPane = findOptionPane().using(robot);
            optionPane.buttonWithText("No").click();
        } catch (WaitTimedOutError ex) {}
        JFileChooserFixture fileChooser = findFileChooser().using(robot);
        fileChooser.requireVisible();
        fileChooser.cancel();
    }

    @Test
    public void testLoadSetsStatusBar(){
        ComponentFinder newFinder = robot.finder();
        frame.button("loadButton").click();
        try {
            // in case unsaved current game
            JOptionPaneFixture optionPane = findOptionPane().using(robot);
            optionPane.buttonWithText("No").click();
        } catch (WaitTimedOutError ex) {}
        JFileChooser chooser = (JFileChooser) newFinder.findByType(JFileChooser.class);
        chooser.setSelectedFile(new File("samplegame.pgn"));
        JButtonMatcher matcher = JButtonMatcher.withText("Open").andShowing();
        JButton openButton = (JButton) newFinder.find(matcher);
        openButton.setEnabled(true);
        JButtonFixture openButtonFix = new JButtonFixture(robot,openButton);
        openButtonFix.click();
        String actualLabel = frame.label("statusLabel").text();
        assertThat(actualLabel).contains("Loading game from file: samplegame.pgn");
    }

    @Test
    public void testSaveButtonCreatesOptionPane(){
        frame.button("newGameButton").click();
        JOptionPaneFixture optionPane = findOptionPane().using(robot);
        try {
            // in case unsaved current game
            optionPane.buttonWithText("No").click();
            optionPane = findOptionPane().using(robot);
        } catch (Exception ex) {}
        optionPane.buttonWithText("White").click();

        frame.button("saveButton").click();
        JFileChooserFixture fileChooser = findFileChooser().using(robot);
        fileChooser.requireVisible();
        fileChooser.cancel();
    }

    @Test
    public void testSaveSetsStatusBar(){
        frame.button("newGameButton").click();
        JOptionPaneFixture optionPane = findOptionPane().using(robot);
        try {
            // in case unsaved current game
            optionPane.buttonWithText("No").click();
            optionPane = findOptionPane().using(robot);
        } catch (Exception ex) {}
        optionPane.buttonWithText("White").click();

        frame.button("saveButton").click();
        JFileChooserFixture fileChooser = findFileChooser().using(robot);
        fileChooser.fileNameTextBox().enterText("newgame");
        fileChooser.approveButton().click();
        String actualLabel = frame.label("statusLabel").text();
        assertThat(actualLabel).contains("Saving game to file: newgame.pgn");
    }

    @Test
    public void testTutorialButtonCreatesOptionPane(){
        frame.button("tutorialButton").click();
        JOptionPaneFixture optionPane = findOptionPane().using(robot);
        optionPane.requireTitle("Tutorial");
        optionPane.okButton().click();
    }

    @Test
    public void testChessPieceMove(){
        ComponentFinder newFinder = robot.finder();
        frame.button("newGameButton").click();
        JOptionPaneFixture optionPane = findOptionPane().using(robot);
        try {
            // in case unsaved current game
            optionPane.buttonWithText("No").click();
            optionPane = findOptionPane().using(robot);
        } catch (Exception ex) {}
        optionPane.buttonWithText("White").click();

        JButton firstSquare = (JButton) newFinder.findByName(frame.target(), "BoardSquare:E,2", BoardSquare.class);
        JButton secondSquare = (JButton) newFinder.findByName(frame.target(), "BoardSquare:E,4", BoardSquare.class);
        JButtonFixture firstSquareFix = new JButtonFixture(robot, firstSquare);
        JButtonFixture secondSquareFix = new JButtonFixture(robot, secondSquare);
        String expectedText = firstSquareFix.text();
        firstSquareFix.click();
        secondSquareFix.click();
        firstSquareFix.requireText(" ");
        secondSquareFix.requireText(expectedText);
    }

    @Test
    public void testCastling(){
        ComponentFinder newFinder = robot.finder();
        frame.button("newGameButton").click();
        JOptionPaneFixture optionPane = findOptionPane().using(robot);
        try {
            // in case unsaved current game
            optionPane.buttonWithText("No").click();
            optionPane = findOptionPane().using(robot);
        } catch (Exception ex) {}
        optionPane.buttonWithText("White").click();

        JButton c5 = (JButton) newFinder.findByName(frame.target(), "BoardSquare:C,5", BoardSquare.class);
        JButton c7 = (JButton) newFinder.findByName(frame.target(), "BoardSquare:C,7", BoardSquare.class);
        JButton e1 = (JButton) newFinder.findByName(frame.target(), "BoardSquare:E,1", BoardSquare.class);
        JButton e2 = (JButton) newFinder.findByName(frame.target(), "BoardSquare:E,2", BoardSquare.class);
        JButton e4 = (JButton) newFinder.findByName(frame.target(), "BoardSquare:E,4", BoardSquare.class);
        JButton e5 = (JButton) newFinder.findByName(frame.target(), "BoardSquare:E,5", BoardSquare.class);
        JButton e7 = (JButton) newFinder.findByName(frame.target(), "BoardSquare:E,7", BoardSquare.class);
        JButton f1 = (JButton) newFinder.findByName(frame.target(), "BoardSquare:F,1", BoardSquare.class);
        JButton f3 = (JButton) newFinder.findByName(frame.target(), "BoardSquare:F,3", BoardSquare.class);
        JButton f5 = (JButton) newFinder.findByName(frame.target(), "BoardSquare:F,5", BoardSquare.class);
        JButton f7 = (JButton) newFinder.findByName(frame.target(), "BoardSquare:F,7", BoardSquare.class);
        JButton g1 = (JButton) newFinder.findByName(frame.target(), "BoardSquare:G,1", BoardSquare.class);
        JButton h1 = (JButton) newFinder.findByName(frame.target(), "BoardSquare:H,1", BoardSquare.class);
        JButtonFixture e1Fix = new JButtonFixture(robot, e1);
        JButtonFixture e2Fix = new JButtonFixture(robot, e2);
        JButtonFixture e4Fix = new JButtonFixture(robot, e4);
        JButtonFixture f1Fix = new JButtonFixture(robot, f1);
        JButtonFixture f3Fix = new JButtonFixture(robot, f3);
        JButtonFixture g1Fix = new JButtonFixture(robot, g1);
        JButtonFixture h1Fix = new JButtonFixture(robot, h1);
        String expectedText = h1Fix.text();
        e2Fix.click();
        e4Fix.click();
        f1Fix.click();
        e2Fix.click();
        g1Fix.click();
        f3Fix.click();
        e1Fix.click();
        g1Fix.click();
        f1Fix.requireText(expectedText);
    }

    @Test
    public void testWhitePieceColorChange(){
        ComponentFinder newFinder = robot.finder();
        JButton whiteSquare = (JButton) newFinder.findByName(frame.target(), "BoardSquare:A,1", BoardSquare.class);
        JButtonFixture squareFix = new JButtonFixture(robot, whiteSquare);
        frame.button("pieceColorButton").click();
        JOptionPaneFixture optionPane = findOptionPane().using(robot);
        optionPane.buttonWithText("White").click();
        JColorChooser chooser = (JColorChooser) newFinder.findByType(JColorChooser.class);
        chooser.setColor(Color.RED);
        JButtonMatcher matcher = JButtonMatcher.withText("OK").andShowing();
        JButtonFixture okButton = new JButtonFixture(robot,(JButton)newFinder.find(matcher));
        okButton.click();
        squareFix.foreground().requireEqualTo(Color.RED);
    }

    @Test
    public void testBlackPieceColorChange(){
        ComponentFinder newFinder = robot.finder();
        JButton blackSquare = (JButton) newFinder.findByName(frame.target(), "BoardSquare:A,8", BoardSquare.class);
        JButtonFixture squareFix = new JButtonFixture(robot, blackSquare);
        frame.button("pieceColorButton").click();
        JOptionPaneFixture optionPane = findOptionPane().using(robot);
        optionPane.buttonWithText("Black").click();
        JColorChooser chooser = (JColorChooser) newFinder.findByType(JColorChooser.class);
        chooser.setColor(Color.BLUE);
        JButtonMatcher matcher = JButtonMatcher.withText("OK").andShowing();
        JButtonFixture okButton = new JButtonFixture(robot,(JButton)newFinder.find(matcher));
        okButton.click();
        squareFix.foreground().requireEqualTo(Color.BLUE);
    }

    @Test
    public void testColorThemeChange(){
        ComponentFinder newFinder = robot.finder();
        frame.button("colorThemeButton").click();
        JOptionPaneFixture optionPane = findOptionPane().using(robot);
        JComboBoxFixture dropMenu = optionPane.comboBox();
        dropMenu.selectItem("Shamrock");
        optionPane.okButton().click();
        JPanelFixture panelFix = frame.panel("historyPanel");
        panelFix.background().requireEqualTo(Color.decode(Core.themes[2][1]));
    }

    @Test
    public void testFlip(){
        ComponentFinder newFinder = robot.finder();
        frame.button("newGameButton").click();
        JOptionPaneFixture optionPane = findOptionPane().using(robot);
        try {
            // in case unsaved current game
            optionPane.buttonWithText("No").click();
            optionPane = findOptionPane().using(robot);
        } catch (Exception ex) {}
        optionPane.buttonWithText("White").click();

        JButton h1Button = (JButton) newFinder.findByName(frame.target(), "BoardSquare:A,1", BoardSquare.class);
        Container board = h1Button.getParent();
        Component[] components = board.getComponents();
        JButton square = (JButton) components[11];
        JButtonFixture squareFix = new JButtonFixture(robot, square);
        squareFix.foreground().requireEqualTo(Color.BLACK);
        frame.button("flipButton").click();
        h1Button = (JButton) newFinder.findByName(frame.target(), "BoardSquare:A,1", BoardSquare.class);
        board = h1Button.getParent();
        components = board.getComponents();
        square = (JButton) components[11];
        squareFix = new JButtonFixture(robot, square);
        squareFix.foreground().requireEqualTo(Color.WHITE);
    }

}