package com.twoforboth.realtimescoring;

import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.ProgressMonitor;

import com.twoforboth.communication.CantConnectException;
import com.twoforboth.communication.ServletInfo;
import com.twoforboth.communication.Servlet;
import com.twoforboth.realtimescoring.data.Score;
import com.twoforboth.realtimescoring.events.GameStatsEvent;
import com.twoforboth.realtimescoring.events.GameStatsListener;
import com.twoforboth.realtimescoring.events.GameStartedEvent;
import com.twoforboth.realtimescoring.events.GameEndedEvent;
import com.twoforboth.realtimescoring.events.GameInitializedEvent;
import com.twoforboth.realtimescoring.Initializor;
import com.twoforboth.realtimescoring.data.FantasyTeam;
import com.twoforboth.realtimescoring.data.FantasyTeamMatchup;
import com.twoforboth.realtimescoring.data.NFLPlayer;
import com.twoforboth.realtimescoring.data.FilledScores;
import com.twoforboth.realtimescoring.data.FilledScore;
import com.twoforboth.realtimescoring.data.NFLTeams;
import com.twoforboth.realtimescoring.controls.LineupTable;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.StringTokenizer;

/**
 * <p>Title: Scrambled Eggs Football Real Time Scoring</p>
 * <p>Description: Real Time Football Updates.  Applet that grabs all of the
 * pertenant information for a particular league for a particular week from a
 * database using servlets.  This information initializes tables that represent
 * fantasy football teams for the week.  Application then creates individual
 * timers to parse NFL games pages generating stats that are used to calculate
 * scores for each fantasy football franchise.  The scores and totals are
 * updated within the tables.
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public class RealTimeScoring extends JApplet implements GameStatsListener
{
  //HTML parameters
  private final String LEAGUE_NAME_PARAM = "LeagueName";
  private final String LEAGUE_YEAR_PARAM = "LeagueYear";
  private final String TEAM_NAME_PARAM = "TeamName";
  private final String PASSWORD_PARAM = "Password";
  private final String DEMO_PARAM = "Demo";
  private final String LOCAL_PARAM = "Local";

  private final String LOG_FILE_NAME = "../realtimescoring/realtimein.txt";

  private final int TABLE_PADX = 125;
  private final int TABLE_PADY = 160;

  private final int PLAYER_NAME_COLUMN = 0;
  private final int PLAYER_TEAM_COLUMN = 1;
  private final int PLAYER_POSITION_COLUMN = 2;
  private final int PLAYER_SCORE_COLUMN = 3;

  private Initializor it_ = new Initializor();

  private LineupTable []homeTeamTables_ = null;
  private LineupTable []awayTeamTables_ = null;

  private final String NAME_HEADER = "Name";
  private final String TEAM_HEADER = "Tm";
  private final String POSITION_HEADER = "Pos";
  private final String POINTS_HEADER = "Pts";

  private final int GAME_ENDED = 0;
  private final int GAME_STARTED = 1;


  private final Dimension LINEUP_DIMENSION = new Dimension(275, 125);
  private final String[] LINEUP_HEADERS = { NAME_HEADER,
                                            TEAM_HEADER,
					    POSITION_HEADER,
					    POINTS_HEADER };
  private final int[] LINEUP_COLUMN_WIDTHS = { 130, 55, 45, 45 };

  private Color NEGATIVE_BLINK = Color.red;
  private Color POSITIVE_BLINK = Color.cyan;

  private StringTokenizer st_ = null;

  private final String[] COLORED_COLUMNS = { NAME_HEADER,
                                             TEAM_HEADER,
					     POSITION_HEADER };

  private Servlet updateLogServlet_ =
      new Servlet(ServletInfo.UPDATE_LOG,
		  ServletInfo.UPDATE_LOG_PARAMETERS);   //Servlet to update a log file indicating when user enters and exits application


  private String leagueName_ = "";
  private String leagueYear_ = "";
  private String teamName_ = "";
  private String password_ = "";
  private String demo_ = "";

  private ProgressMonitor pm_ = null;
  private int numberOfGames_ = 0;
  private int gamesChecked_ = 0;

  /**
   * Entry point for applet.  Gets login information.  Attempts to log the user
   * in.  Grabs league / team information from database and initializes the screen.
   */
  public void init()
  {

    if (getParameter(DEMO_PARAM) != null)
    {
      demo_ = getParameter(DEMO_PARAM);
    }

    if (getParameter(LEAGUE_NAME_PARAM) != null)
    {
      leagueName_ = getParameter(LEAGUE_NAME_PARAM);
    }
    if (getParameter(LEAGUE_YEAR_PARAM) != null)
    {
      leagueYear_ = getParameter(LEAGUE_YEAR_PARAM);
    }
    if (getParameter(TEAM_NAME_PARAM) != null)
    {
      teamName_ = getParameter(TEAM_NAME_PARAM);
    }
    if (getParameter(PASSWORD_PARAM) != null)
    {
      password_ = getParameter(PASSWORD_PARAM);
    }

    if (demo_.length() == 0)
    {
      if (!it_.doLogin(leagueYear_,
		       leagueName_,
		       teamName_,
                       password_))
      {
	System.out.println("Login NOT OK");
	System.exit(0);
      }
    }
    pm_ = new ProgressMonitor(this.getContentPane(),
			      "Initializing data",
			      "",
			      0,
			      1);
    //Initialize teams, players, score rules, etc.
    it_.doInit(leagueName_, leagueYear_);
    pm_.close();
    numberOfGames_ = it_.getGameCheckersSize();
    //System.out.println("NUMBER OF GAMES = " + numberOfGames_);
    //Build the screen
    initGUI();
    //Put up the Progressmonitor and start check thread
    pm_ = new ProgressMonitor(this.getContentPane(),
			      "Retrieving Stats For Game:",
			      "",
			      0,
			      numberOfGames_);
    //Create the NFL game checker threads
    createNFLGameCheckerThreads();
  }

  /**
   * Method called when applet is destroyed.  Updates log file
   */
  public void destroy()
  {
    StringBuffer sb = new StringBuffer();
    StringBuffer text = new StringBuffer("Exited ");

    if (getParameter(LOCAL_PARAM) == null)
    {
      if (teamName_.length() > 0)
      {
	text.append('(');
	text.append(leagueName_);
	text.append(')');
	text.append(' ');
	text.append('(');
	text.append(leagueYear_);
	text.append(')');
	text.append(' ');
	text.append('(');
	text.append(teamName_);
	text.append(')');
      }
      else
      {
	text.append("Guest");
      }

      sb.append(LOG_FILE_NAME);
      sb.append(ServletInfo.DELIMITER);
      sb.append(text.toString());

      try
      {
//	updateLogServlet_.setDebug(true);
	updateLogServlet_.execute(sb.toString());
//	updateLogServlet_.setDebug(false);
      }
      catch (CantConnectException cce)
      {
	System.out.println("Error writing to logfile");
      }
    }
  }

  /**
   * Creates a thread for each NFL game checker.
   */

  private void createNFLGameCheckerThreads()
  {
    NFLGameChecker gc = null;

    //Note -- each game checker should actually be a thread and should be staggered and
    //generate events when the parsing is complete
    Thread t[] = new Thread[numberOfGames_];
    int threadIndex = 0;
    it_.setFirstGameChecker();
    while ((gc = it_.getNextGameChecker()) != null)
    {
      //Make this applet listen for updates for this game
      gc.addGameStatsListener(this);
      //Create the new thread
      t[threadIndex] = new Thread(gc);
      //Make it a daemon (i.e. it will stop when the applet is closed)
      t[threadIndex].setDaemon(true);
      //Start up the thread
      t[threadIndex].start();
      threadIndex++;
    }
  }

  /**
   * Sets up and draws the screen
   */

  private void initGUI()
  {
    int i = 0;
    int max = it_.getFantasyTeamMatchupsCount();

    ArrayList topTeamLineup = null;
    ArrayList bottomTeamLineup = null;
    FantasyTeam []homeTeams = new FantasyTeam[max];
    FantasyTeam []awayTeams = new FantasyTeam[max];
    FantasyTeamMatchup ftm;

    for (i = 0; i < max; i++)
    {
      ftm = it_.getFantasyTeamMatchup(i);
      homeTeams[i] = ftm.getHome();
      awayTeams[i] = ftm.getAway();
    }
    buildScreen(homeTeams, awayTeams);
  }

  /**
   * Draws the screen.
   * @param homeTeams Array of fantasy teams -- appear at top of screen
   * @param awayTeams Array of fantasy teams -- appear at bottom of screen
   */

  private void buildScreen(FantasyTeam []homeTeams, FantasyTeam []awayTeams)
  {
    JLabel jLabel = null;
    ArrayList lineup = null;

    LineupTable st = null;
    homeTeamTables_ = new LineupTable[homeTeams.length];
    awayTeamTables_ = new LineupTable[awayTeams.length];

    JScrollPane scrollPane = null;

    GridBagLayout gbl = new GridBagLayout();
    this.getContentPane().setLayout(gbl);
    this.getContentPane().setBackground(new Color(255, 255, 204));
    for (int col = 0; col < homeTeams.length; col++)
    {
      //System.out.println("col = " + col);
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = col;
      gbc.gridy = 0;
      gbc.gridwidth = 1;
      gbc.gridheight = 1;
      gbc.ipadx = 0;
      gbc.ipady = 0;
      gbc.fill = gbc.NONE;
      jLabel = new JLabel(homeTeams[col].getName());

      gbl.setConstraints(jLabel, gbc);
      //jLabel.setBorder(new LineBorder(Color.black));
      this.getContentPane().add(jLabel);

      //Put homeTeamLineups here in the tables
      lineup = homeTeams[col].getWeeklyLineup();
      st = new LineupTable(homeTeams[col].getName(),
			   LINEUP_DIMENSION,
			   LINEUP_HEADERS,
			   LINEUP_COLUMN_WIDTHS,
			   POINTS_HEADER,
			   3,
			   1,
			   NEGATIVE_BLINK,
			   POSITIVE_BLINK,
			   COLORED_COLUMNS);

      scrollPane = new JScrollPane(st);

      homeTeamTables_[col] = st;
      fillTable(st, lineup);
      gbc.gridy = 1;
      gbc.gridwidth = 1;
      gbc.gridheight = 1;
      gbc.ipadx = TABLE_PADX;
      gbc.ipady = TABLE_PADY;
      gbc.fill = gbc.BOTH;
      gbl.setConstraints(scrollPane, gbc);
      this.getContentPane().add(scrollPane);

      gbc.gridy = 2;
      gbc.gridwidth = 1;
      gbc.gridheight = 1;
      gbc.ipadx = 0;
      gbc.ipady = 0;
      gbc.fill = gbc.NONE;

      jLabel = new JLabel(awayTeams[col].getName());
      gbl.setConstraints(jLabel, gbc);
      //jLabel.setBorder(new LineBorder(Color.black));
      this.getContentPane().add(jLabel);

      //Put the awayTeamLineups here in the tables
      lineup = awayTeams[col].getWeeklyLineup();
      st = new LineupTable(awayTeams[col].getName(),
			   LINEUP_DIMENSION,
			   LINEUP_HEADERS,
			   LINEUP_COLUMN_WIDTHS,
			   POINTS_HEADER,
			   3,
			   1,
			   NEGATIVE_BLINK,
			   POSITIVE_BLINK,
			   COLORED_COLUMNS);

      scrollPane = new JScrollPane(st);
      awayTeamTables_[col] = st;
      fillTable(st, lineup);
      gbc.gridy = 3;
      gbc.gridwidth = 1;
      gbc.gridheight = 1;
      gbc.ipadx = TABLE_PADX;
      gbc.ipady = TABLE_PADY;
      gbc.fill = gbc.BOTH;
      gbl.setConstraints(scrollPane, gbc);
      this.getContentPane().add(scrollPane);
    }
  }

  /**
   * Fills each fantasy team table with that fantasy team's lineup
   * @param lt Lineup table
   * @param lineup Array of NFLPlayers that make up the fantasy team's lineup
   */

  private void fillTable(LineupTable lt, ArrayList lineup)
  {
    int max = lineup.size();
    Integer zero = new Integer(0);
    Object []row = new Object[4];
    String colorRGB = new Integer(Color.yellow.getRGB()).toString();
    String whiteRGB = new Integer(Color.white.getRGB()).toString();
    String redRGB = new Integer(Color.red.getRGB()).toString();
    StringBuffer sb = new StringBuffer();
    StringBuffer sbColor = new StringBuffer();

    for (int i = 0; i != max; i++)
    {
      //Get the player
      NFLPlayer player = (NFLPlayer)lineup.get(i);
      //Get the team
      String playerTeam = player.getNFLTeam();
      //Convert to 3 character name
      if (playerTeam.length() == 2)
      {
	playerTeam = NFLTeams.getThreeCharacterShortName(playerTeam);
      }

      //Determine if the player is on a bye.  If so, paint red
      if (it_.nflTeamOnBye(playerTeam))
      {
	sbColor.setLength(0);
	sbColor.append('~');
	sbColor.append(redRGB);
      }
      else    //Paint yellow
      {
	sbColor.setLength(0);
	sbColor.append('~');
	sbColor.append(colorRGB);
      }

      //Display the name in the first column
      sb.setLength(0);
      sb.append(player.getName());
      sb.append(sbColor.toString());
      row[0] = sb.toString();

      //Display the nfl team in the second column
      sb.setLength(0);
      sb.append(playerTeam);
      sb.append(sbColor.toString());
      row[1] = sb.toString();

      //Display the position in the third column
      sb.setLength(0);
      sb.append(player.getPosition());
      sb.append(sbColor.toString());
      row[2] = sb.toString();

      //Display the 0 as the score
      row[3] = zero;
      lt.addRow(row);
    }
    //Add the total row
    sb.setLength(0);
    sb.append("Total");
    sb.append('~');
    sb.append(whiteRGB);
    row[0] = sb.toString();
    sb.setLength(0);
    sb.append(' ');
    sb.append('~');
    sb.append(whiteRGB);
    row[1] = sb.toString();
    row[2] = sb.toString();
    row[3] = zero;
    lt.addRow(row);
  }

  /**
   * Updates the scores and totals for a particular score
   * @param fs FilledScore object
   * @param initializing True if in initializing mode
   */

  private void updateTableScores(FilledScore fs, boolean initializing)
  {
    int i = 0;
    int max = homeTeamTables_.length;
    String name = fs.getFantasyTeamName();
    LineupTable table = null;
    //Get correct table from fantasy team
    for (i = 0; i < max; i++)
    {
      if (name.equalsIgnoreCase(homeTeamTables_[i].getName()))
      {
	table = homeTeamTables_[i];
	break;
      }
      else if (name.equalsIgnoreCase(awayTeamTables_[i].getName()))
      {
	table = awayTeamTables_[i];
	break;
      }
    }
    //Next find the correct name in the table matching the stat name
    max = table.getRowCount();
    String playerName = fs.getPlayerName();
    String alternateName = fs.getPlayerNameNoInitial();
    String position = fs.getPosition();
    String tablePlayerName = "";
    String tablePlayerPosition = "";
    String tableNameNoInital = "";
    String originalCellValue = "";
    int originalValue = 0;
    for (i = 0; i < max; i++)
    {
      //Note: Cell will contain data~color
      originalCellValue = (String)table.getValueAt(i, PLAYER_NAME_COLUMN);
      try
      {
	st_ = new StringTokenizer(originalCellValue, "~", false);
	tablePlayerName = st_.nextToken();
      }
      catch (Exception e)
      {
	tablePlayerName = originalCellValue;
      }

      if (tablePlayerName.indexOf(',') != -1)
      {
	StringTokenizer stringtokenizer = new StringTokenizer(tablePlayerName, ",", false);
	String lastName = stringtokenizer.nextToken().trim();
	String firstName = stringtokenizer.nextToken().trim();
	String firstNameNoInitial = firstName;
	if (firstName.endsWith("."))   //Have a middle initial try without
	{
	  firstNameNoInitial = firstName.substring(0, firstName.length() - 3);
	}
	tablePlayerName = firstName + " " + lastName;
	tableNameNoInital = firstNameNoInitial + " " + lastName;
      }

      //Note: Cell will contain data~color
      originalCellValue = (String)table.getValueAt(i, PLAYER_POSITION_COLUMN);
      try
      {
	st_ = new StringTokenizer(originalCellValue, "~", false);
	tablePlayerPosition = st_.nextToken();
      }
      catch (Exception e)
      {
	tablePlayerPosition = originalCellValue;
      }


      if ((tablePlayerName.equalsIgnoreCase(playerName) ||
	   tableNameNoInital.equalsIgnoreCase(alternateName)) &&
	   tablePlayerPosition.equalsIgnoreCase(position))
      {
	try
	{
	  originalValue = ((Integer)table.getValueAt(i, PLAYER_SCORE_COLUMN)).intValue();
	}
	catch (Exception e)
	{
	  originalValue = 0;
	}
	if (originalValue != fs.getPoints())
	{
	  if (initializing)        //Don't blink on init
	  {
	    table.setValueAt(new Integer(fs.getPoints()), i, PLAYER_SCORE_COLUMN);
	  }
	  else
	  {
	    table.updateScoreValue(fs.getPoints(), i, PLAYER_SCORE_COLUMN);
	  }
	  updateTotal(table);
	}
	break;
      }
    }
  }

  /**
   * Updates the total row for a particular table
   * @table LineupTable object
   */

  private void updateTotal(LineupTable table)
  {
    int max = table.getRowCount() - 1;
    int total = 0;
    int rowVal;
    int i;

    for (i = 0; i < max ; i++)
    {
      try
      {
	rowVal = ((Integer)table.getValueAt(i, PLAYER_SCORE_COLUMN)).intValue();
      }
      catch (Exception e)
      {
	rowVal = 0;
      }
      total += rowVal;
    }
    table.setValueAt(new Integer(total), i, PLAYER_SCORE_COLUMN);
//    table.updateScoreValue(total, i, PLAYER_SCORE_COLUMN);
  }

  /**
   * NFLGameChecker triggers event when new stats are read in
   * from the web pages
   * @param evt GameStatsEvent Source of event
   */

  public void newStatsArrived(GameStatsEvent evt)
  {
    //Get the associated game checker object from the event
    NFLGameChecker gc = evt.getGameNFLChecker();
    //Determine if this is the first set of stats for this game
    boolean init = gc.getInit();
    //Generate the filled scores for the game
    it_.doScoresForGame(gc);
    //Get the array of filled scores for the game
    FilledScores filledScores = it_.getFilledScoresForGame(gc);
    //point to beginning of array
    filledScores.first();
    FilledScore fs = null;
    while ((fs = filledScores.getNext()) != null)
    {
      updateTableScores(fs, init);
    }
  }

  /**
   * Event generated when a game has started.  Calls method to update tables
   * indicating that this particular game has ended.
   * @param evt GameStatsEvent Source of event
   */

  public void gameStarted(GameStartedEvent evt)
  {
    //Get the associated game checker object from the event
    NFLGameChecker gc = evt.getGameNFLChecker();
    //Get the names of each team involved in this game
    String homeTeamName = gc.getHomeTeamShortName();
    String visitingTeamName = gc.getVisitingTeamShortName();
    //Mark the cells for this game indicated that it started
    updateTableGameState(homeTeamName, visitingTeamName, GAME_STARTED);
  }

  /**
   * Event generated when a game has ended.  Calls method to update tables
   * indicating that this particular game has ended.
   * @param evt GameStatsEvent Source of event
   */

  public void gameEnded(GameEndedEvent evt)
  {
    //Get the associated game checker object from the event
    NFLGameChecker gc = evt.getGameNFLChecker();
    //Get the names of each team involved in this game
    String homeTeamName = gc.getHomeTeamShortName();
    String visitingTeamName = gc.getVisitingTeamShortName();

    //System.out.println("gameEnded event caught for teams " + homeTeamName + " vs " + visitingTeamName);

    //Mark the cells for this game indicated that it started
    updateTableGameState(homeTeamName, visitingTeamName, GAME_ENDED);
  }

  /**
   * Listener for event when a game is first read into the system from
   * the NFLGameChecker.  Method updates the progress monitor.
   */

  public void gameInitialized(GameInitializedEvent evt)
  {
    pm_.setNote(evt.getGameNFLChecker().getHomeTeamLongName() + " vs. " +
		 evt.getGameNFLChecker().getVisitingTeamLongName());
    gamesChecked_++;
    pm_.setProgress(gamesChecked_);
    if (gamesChecked_ >= numberOfGames_)
    {
      pm_.close();
    }
  }

  /**
   * Modifies the non-scoring table cells indicating the state of the NFL game
   * @param homeTeamName Name of home team
   * @param visitingTeamName Name of visiting team
   * @param state
   */

  private void updateTableGameState(String homeTeamName,
				    String visitingTeamName,
				    int state)
  {
    String colorRGB = "";
    String tablePlayerTeam = "";
    String cellValue;
    StringBuffer newValue = null;
    String newCellValue;

    if (state == GAME_STARTED)
    {
      colorRGB = new Integer(Color.green.getRGB()).toString();
    }
    else if (state == GAME_ENDED)
    {
      colorRGB = new Integer(Color.white.getRGB()).toString();
    }

    if (colorRGB.length() > 0)
    {
      int max = homeTeamTables_.length;
      LineupTable table = null;

      for (int j = 0; j < 2; j++)
      {
	for (int i = 0; i < max; i++)
	{
	  if (j == 0)
	  {
	     table = homeTeamTables_[i];
    	  }
	  else
	  {
	    table = awayTeamTables_[i];
	  }
	  for (int k = 0; k < table.getRowCount(); k++)
	  {
	    cellValue = (String)table.getValueAt(k, PLAYER_TEAM_COLUMN);
	    try
	    {
	      st_ = new StringTokenizer(cellValue, "~", false);
	      tablePlayerTeam = st_.nextToken();
	    }
	    catch (java.util.NoSuchElementException nsee)
	    {
	      tablePlayerTeam = cellValue;
	    }

	    if (tablePlayerTeam.equalsIgnoreCase(homeTeamName) ||
		tablePlayerTeam.equalsIgnoreCase(visitingTeamName))
	    {
	      newValue = new StringBuffer(tablePlayerTeam);
	      newValue.append('~');
	      newValue.append(colorRGB);
	      table.setValueAt(newValue.toString(), k, PLAYER_TEAM_COLUMN);

	      //Update the other cells
	      for (int l = 0 ; l < COLORED_COLUMNS.length; l++)
	      {
		int index = table.getColumn(COLORED_COLUMNS[l]).getModelIndex();
		if (index != PLAYER_TEAM_COLUMN)
		{
		  cellValue = (String)table.getValueAt(k, index);
		  try
		  {
		    st_ = new StringTokenizer(cellValue, "~", false);
		    newCellValue = st_.nextToken();
		  }
		  catch (java.util.NoSuchElementException nsee)
		  {
		    newCellValue = cellValue;
		  }

		  newValue = new StringBuffer(newCellValue);
		  newValue.append('~');
		  newValue.append(colorRGB);
		  table.setValueAt(newValue.toString(), k, index);
		}
	      }
	    }
	  }
	}
      }
    }
  }
}


