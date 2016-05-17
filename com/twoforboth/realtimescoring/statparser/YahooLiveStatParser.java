package com.twoforboth.realtimescoring.statparser;

import com.twoforboth.realtimescoring.data.Score;
import com.twoforboth.realtimescoring.data.ScoringRule;
import com.twoforboth.realtimescoring.data.NFLPlayer;

import com.twoforboth.util.Misc;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * <p>Title: Scrambled Eggs Football Real Time Scoring</p>
 * <p>Description: Real Time Football Updates</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public class YahooLiveStatParser extends YahooBaseParser implements StatParser
{
//  private final String SCORE_AREA_TOTAL = "TOTAL";
  private final String TEAM_DIRECTORY_PREFIX = "NFL/TEAMS/";

  private final String MAJOR_KEY_SCORING_SUMMARY = "SCORING SUMMARY";
  private final String MAJOR_KEY_LIVE_BOX_SCORE = "LIVE BOX SCORE";

  private final String PASSES_TO_SPACE_STRING = "PASSES TO ";
  private final String TO_THE_STRING = "TO THE";
  private final String DOWN_THE_STRING = "DOWN THE";

  private final String STRING_FAILED = "FAILED";

  private final String TEAM_AREA_PASSING = "PASSING";
  private final String TEAM_AREA_RECEIVING = "RECEIVING";
  private final String TEAM_AREA_RUSHING = "RUSHING";
  private final String TEAM_AREA_KICKING = "KICKING";

  private String teamAreaKeys_[] = { TEAM_AREA_PASSING,
                                     TEAM_AREA_RECEIVING,
				     TEAM_AREA_RUSHING,
				     TEAM_AREA_KICKING };

  private String majorKeys_[] = { MAJOR_KEY_SCORING_SUMMARY,
			  MAJOR_KEY_LIVE_BOX_SCORE,
			  LIVE_BOX_FUMBLES_LOST };

  private String liveBoxScoreKeys_[] = { LIVE_BOX_SACKED_YARDS_LOST,
				 LIVE_BOX_HAD_INTERCEPTED,
				 LIVE_BOX_FUMBLES_LOST };

  private String homeTeamShortname_ = "";
  private String awayTeamShortname_ = "";

  /**
   * Constructor
   * @param homeTeam Full name for home team
   * @param awayTeam Full name for away team
   * @buffer Page to parse
   * @param homeTeamShortname short name for hometeam
   * @param awayTeamShortname short name for awayteam
   */

  public YahooLiveStatParser(String homeTeam,
			     String awayTeam,
			     String buffer,
			     String homeTeamShortname,
			     String awayTeamShortname)
  {
    super(getYahooLongName(homeTeam), getYahooLongName(awayTeam), buffer);
    homeTeamShortname_ = homeTeamShortname.toUpperCase();
    awayTeamShortname_ = awayTeamShortname.toUpperCase();
  }

  public ArrayList getScoresForPlayer(String name, String nflTeam, String nameNoMiddleInitial)
  {
    return super.getScoresForPlayer(name, nflTeam, nameNoMiddleInitial);
  }

  /**
   * @returns The next score object or null
   */

  public Score getNextScore()
  {
    return super.getNextScore();
  }

  /**
   * Parses the 4 main sections of the page
   */

  public boolean parse()
  {
    boolean retval = false;
    retval = super.parse();
    if (documentNotFound_ == false)
    {
      retval = parseScoreArea();
      if (retval)
      {
	retval = parseScoringSummary();
      }
      if (retval)
      {
	retval = parseLiveBoxScore(majorKeys_, liveBoxScoreKeys_);
      }
      if (retval)
      {
	retval = parseTeamArea();
      }
    }
    return retval;
  }

  private boolean parseScoreArea()
  {
    boolean retval = true;
    int endIndex = 0;
    int awayPoints = 0;
//    System.out.println("Looking for awayTeam_ = <" + awayTeam_ + ">");

///nfl/teams/nor/
    String homeSearch = TEAM_DIRECTORY_PREFIX + homeTeamShortname_;
    String awaySearch = TEAM_DIRECTORY_PREFIX + awayTeamShortname_;

    currentCharacterIndex_ = buffer_.indexOf(SCORE_AREA_TOTAL);
    currentCharacterIndex_ = buffer_.indexOf(awaySearch, currentCharacterIndex_);
    currentCharacterIndex_ = buffer_.indexOf(HTML_BOLD_START, currentCharacterIndex_) + HTML_BOLD_START.length();
    endIndex = buffer_.indexOf(HTML_BOLD_END, currentCharacterIndex_);
    try
    {
      awayPoints = new Integer(buffer_.substring(currentCharacterIndex_, endIndex)).intValue();
    }
    catch (Exception e1)
    {
      //System.out.println("parseScoreArea A homeSearch = " + homeSearch + " awaySearch = " + awaySearch + " buffer = " + buffer_);
      retval = false;
    }

//    System.out.println("Looking for homeTeam_ = <" + homeTeam_ + ">");
    currentCharacterIndex_ = buffer_.indexOf(homeSearch, currentCharacterIndex_);
    currentCharacterIndex_ = buffer_.indexOf(HTML_BOLD_START, currentCharacterIndex_) + HTML_BOLD_START.length();
    endIndex = buffer_.indexOf(HTML_BOLD_END, currentCharacterIndex_);
    int homePoints = 0;
    try
    {
      homePoints = new Integer(buffer_.substring(currentCharacterIndex_, endIndex)).intValue();
    }
    catch (Exception e1)
    {
      //System.out.println("parseScoreArea B homeSearch = " + homeSearch + " awaySearch = " + awaySearch + " buffer = " + buffer_);
      retval = false;
    }


    createAndAddScore(homeTeam_ + EXTRA_DEFENSE,
		      homeTeam_,
		      ScoringRule.DEFENSE_WEEKLY_POINTS_ALLOWED,
		      awayPoints);
    createAndAddScore(awayTeam_ + EXTRA_DEFENSE,
		      awayTeam_,
		      ScoringRule.DEFENSE_WEEKLY_POINTS_ALLOWED,
		      homePoints);
    return retval;
  }

  /**
   * Parses the area in between the text "Scoring Summary" and "Live Box Score"
   */

  private boolean  parseScoringSummary()
  {
    boolean retval = true;
    String currentNFLTeam = "";
    String touchdownText = "";
    String extraPointText = "";


    //Move cursor to first key (i.e. Scoring Summary)
    currentCharacterIndex_ = buffer_.indexOf(majorKeys_[0]);
    //Set the next buffer to the next major key
    nextCharacterIndex_ = buffer_.indexOf(majorKeys_[1]);
    //Next loop while currentCharacterIndex_ < nextCharacterIndex_
    while (currentCharacterIndex_ <= nextCharacterIndex_)
    {
      //Look for lines that contain either homeTeam_ or awayTeam_
      int index = nextTeam(currentCharacterIndex_, " ");
      if (index == -1)
      {
	break;
      }
      else
      {
	currentCharacterIndex_ = index;
      }
      if (currentCharacterIndex_ >= nextCharacterIndex_)
      {
	break;
      }
      //Chicago Field Goal</b> - Paul Edinger kicks a 31-yard
      currentNFLTeam = lastTeam_;
      currentCharacterIndex_ += currentNFLTeam.length() + 1;   //1 for the space
      //Score type
      String scoreTypeText = buffer_.substring(currentCharacterIndex_, buffer_.indexOf('<', currentCharacterIndex_));
      currentCharacterIndex_ += scoreTypeText.length();
      //Score text
      String fullScoreLine = buffer_.substring(currentCharacterIndex_, buffer_.indexOf(':', currentCharacterIndex_));
      currentCharacterIndex_ += fullScoreLine.length();

      fullScoreLine = fullScoreLine.substring(fullScoreLine.indexOf("-") + 1).trim();
      //System.out.println("fullScoreLine = <" + fullScoreLine + ">");

      //Determine which type
      //Have scoreTypeText
      if (scoreTypeText.equalsIgnoreCase(STRING_FIELD_GOAL))
      {
	StringBuffer sb = new StringBuffer(fullScoreLine);
	sb = Misc.stripHTML(sb);
	fullScoreLine = sb.toString();
	createFieldGoalScore(fullScoreLine, currentNFLTeam);
      }
      else if (scoreTypeText.equalsIgnoreCase(STRING_SAFETY))
      {
	//NOTE -- THEY SEEM TO BE REPORTING THIS BACKWARDS -- FOR NOW I'll STAND MY GROUND
	createAndAddScore(currentNFLTeam + EXTRA_DEFENSE,
			  currentNFLTeam,
			  ScoringRule.DEFENSE_WEEKLY_SAFTIES,
			  1);

      }
      else  //Some form of a touchdown
      {
	  //Break up the Touchdown text from the extra point text
	  //There is a possibility of there being two sets of ('s -- if so choose the last set
	int lastIndexOpen = 0;
	int lastIndexClose = 0;

	try
	{
	  lastIndexOpen = fullScoreLine.lastIndexOf('(');
	  lastIndexClose = fullScoreLine.lastIndexOf(')');

	  touchdownText =
	      fullScoreLine.substring(0, lastIndexOpen);
	  extraPointText =
	      fullScoreLine.substring(lastIndexOpen + 1,
				      lastIndexClose);
	}
	catch (java.lang.StringIndexOutOfBoundsException e)
	{
	  break;
	}

	StringBuffer sb = new StringBuffer(touchdownText);
	sb = Misc.stripHTML(sb);
	touchdownText = sb.toString();
//	  touchdownText = touchdownText.substring(touchdownText.indexOf("-") + 1).trim();

//	System.out.println("TOUCHDOWN TEXT = " + touchdownText + " EXTRA POINT TEXT = " + extraPointText);

	if (touchdownText.indexOf(STRING_PASS) != -1)
	{
	  createTouchdownPassPlayScore(touchdownText, currentNFLTeam);
	}
	else if (touchdownText.indexOf(STRING_RUN) != -1)
	{
	  parseCreateAddScore(touchdownText,
			      currentNFLTeam,
			      ScoringRule.PLAYER_WEEKLY_RUSHING_TDS,
			      1,
			      EXTRA_NONE,
			      true);
	}

	else if (touchdownText.indexOf(STRING_FUMBLE) != -1)
	{

	  createAndAddScore(currentNFLTeam + EXTRA_DEFENSE,
			      currentNFLTeam,
			      ScoringRule.DEFENSE_WEEKLY_DEFENSIVE_TDS_SCORED,
			      1);
	}
	else if (touchdownText.indexOf(STRING_INTERCEPTION) != -1)
	{
	  createAndAddScore(currentNFLTeam + EXTRA_DEFENSE,
			      currentNFLTeam,
			      ScoringRule.DEFENSE_WEEKLY_DEFENSIVE_TDS_SCORED,
			      1);
	}
	else if (touchdownText.indexOf(STRING_KICKOFF) != -1)
	{
	  createAndAddScore(currentNFLTeam + EXTRA_SPECIAL_TEAMS,
			      currentNFLTeam,
			      ScoringRule.SPECIALS_WEEKLY_KICKOF_RETURN_TDS,
			      1);

	}
	else if (touchdownText.indexOf(STRING_PUNT) != -1)
	{
	  createAndAddScore(currentNFLTeam + EXTRA_SPECIAL_TEAMS,
			      currentNFLTeam,
			      ScoringRule.SPECIALS_WEEKLY_PUNT_RETURN_TDS,
			      1);
	}
	else if (touchdownText.indexOf(STRING_BLOCK) != -1)
	{
	  createAndAddScore(currentNFLTeam + EXTRA_SPECIAL_TEAMS,
			      currentNFLTeam,
			      ScoringRule.SPECIALS_WEEKLY_BLOCK_KICK_RET_TDS,
			      1);

	}
	//Now handle extra point scores
	if (extraPointText.indexOf(STRING_FAILED) != -1)
	{
	  //Don't create a stat
	}
	else if (extraPointText.indexOf(STRING_KICK) != -1)
	{
	  //System.out.println("KICK text = <" + extraPointText + "> -- looking for " + STRING_KICK);
	  //extraPointText = extraPointText.substring(0, extraPointText.indexOf(STRING_KICK) - 1);

	  parseCreateAddScore(extraPointText,
			      currentNFLTeam,
			      ScoringRule.PLAYER_WEEKLY_PAT_MADE,
			      1,
			      EXTRA_NONE,
			      true);
	}

	else if (extraPointText.indexOf(STRING_PASS_TO) != -1)
	{
//(2PT ATTEMPT CONVERTED, JAKE PLUMMER PASS TO FRANK SANDERS)
	  extraPointText = extraPointText.substring(0, extraPointText.indexOf(STRING_TWO_POINT_CONVERTED) + STRING_TWO_POINT_CONVERTED.length());
	  createTwoPointConversionPass(extraPointText, currentNFLTeam);
	}
	else if (extraPointText.indexOf(STRING_RUN) != -1)   //?
	{
//	  System.out.println("About to create 2 point run play");
	  extraPointText = extraPointText.substring(0, extraPointText.indexOf(STRING_RUN) - 1);
	  parseCreateAddScore(extraPointText,
			      currentNFLTeam,
			      ScoringRule.PLAYER_WEEKLY_RUSHING_2PT,
			      1,
			      EXTRA_NONE,
			      true);
	}
    }
/*
      System.out.println("currentIndex = " + currentCharacterIndex_ +
			 " nextCharacterIndex_ = " + nextCharacterIndex_);
*/
    }
    return retval;
  }

  private void createFieldGoalScore(String fullScoreLine, String currentNFLTeam)
  {
    String name = fullScoreLine.substring(0, fullScoreLine.indexOf(STRING_KICK));
    createAndAddScore(NFLPlayer.fixName(name).trim(),
		      currentNFLTeam,
		      ScoringRule.PLAYER_WEEKLY_FG_MADE,
		      1);
  }

  private void createTouchdownPassPlayScore(String scoreLine, String currentNFLTeam)
  {
    //Form is Passer PASSES TO Receiver

    int nextIndex = 0;
    String throwerName = scoreLine.substring(0, scoreLine.indexOf(PASSES_TO_SPACE_STRING));
    nextIndex = scoreLine.indexOf(TO_THE_STRING);
    if (nextIndex == -1)
    {
      nextIndex = scoreLine.indexOf(DOWN_THE_STRING);
      if (nextIndex == -1)
      {
	System.out.println("new string found for pass play " + scoreLine);
      }
    }
    String receiverName = scoreLine.substring(scoreLine.indexOf(PASSES_TO_SPACE_STRING) + PASSES_TO_SPACE_STRING.length(),
					      nextIndex);

    //Now create 2 score items (one for thrower and one for receiver)
    createAndAddScore(NFLPlayer.fixName(receiverName.toString()).trim(),
		      currentNFLTeam,
		      ScoringRule.PLAYER_WEEKLY_RECEIVING_TDS,
		      1);
    createAndAddScore(NFLPlayer.fixName(throwerName.toString()).trim(),
		      currentNFLTeam,
		      ScoringRule.PLAYER_WEEKLY_PASSING_TDS,
		      1);
  }

  private boolean parseTeamArea()
  {
    boolean retval = true;
    int start = 0;
    int end = 0;

    int keyStarts[] = new int[teamAreaKeys_.length];
    int keyEnds[] = new int[teamAreaKeys_.length];
    int tableDatas[] = { 4, 2, 2 };

    StringBuffer workingStringBuffer = new StringBuffer();
    String workingString = "";

    String startWord = "";
    String endWord = "";
    String statLine = "";

    String currentTeam = "";

    int scoringTypes[] = { ScoringRule.PLAYER_WEEKLY_PASSING_YDS,
                           ScoringRule.PLAYER_WEEKLY_RECEIVING_YDS,
			   ScoringRule.PLAYER_WEEKLY_RUSHING_YDS };

//    System.out.println("awayTeam_ = " + awayTeam_ + " homeTeam_ = " + homeTeam_);

    int localCharacterIndex = 0;
    //Away then Home
    for (int i = 0; i < 2; i++)
    {
      if (i == 0)
      {
	 currentCharacterIndex_ = buffer_.indexOf(awayTeam_, currentCharacterIndex_);
	 nextCharacterIndex_ = buffer_.indexOf(homeTeam_, currentCharacterIndex_);
	 try
	 {
	   workingStringBuffer.append(buffer_.substring(currentCharacterIndex_, nextCharacterIndex_));
	 }
	 catch (Exception ex1)
	 {
/*
	   System.out.println("parseTeamArea() i == " + i + " awayTeam = <" + awayTeam_ +
			      "> homeTeam = <" + homeTeam_ + "> buffer = " + buffer_);
*/
	   retval = false;
	 }
	 workingString = workingStringBuffer.toString();
	 currentTeam = awayTeam_;
      }
      else
      {
	currentCharacterIndex_ = nextCharacterIndex_;
	nextCharacterIndex_ = buffer_.indexOf(teamAreaKeys_[teamAreaKeys_.length - 1], currentCharacterIndex_);
	workingStringBuffer.setLength(0);
	workingStringBuffer.append(buffer_.substring(currentCharacterIndex_, nextCharacterIndex_ + teamAreaKeys_[teamAreaKeys_.length - 1].length()));
	workingString = workingStringBuffer.toString();
	currentTeam = homeTeam_;
      }


      for (int j = 0; j < teamAreaKeys_.length - 1; j++)
      {
	keyStarts[j] = workingString.indexOf(teamAreaKeys_[j]);
	keyEnds[j] = workingString.indexOf(teamAreaKeys_[j + 1]);
      }

      localCharacterIndex = 0;
      for (int k = 0; k < keyStarts.length - 1; k++)
      {
	statLine = workingString.substring(keyStarts[k], keyEnds[k]);
	parseTeamAreaLine(statLine, tableDatas[k], scoringTypes[k], currentTeam);
      }
    }
    return retval;
  }

/*
RECEIVING</FONT></TD><TD><FONT FACE=ARIAL SIZE=-2 COLOR=WHITE>REC</FONT>
</TD><TD><FONT FACE=ARIAL SIZE=-2 COLOR=WHITE>YDS</FONT></TD><TD>
<FONT FACE=ARIAL SIZE=-2 COLOR=WHITE>AVG</FONT></TD><TD>
<FONT FACE=ARIAL SIZE=-2 COLOR=WHITE>TD</FONT></TD><TD>
<FONT FACE=ARIAL SIZE=-2 COLOR=WHITE>FUML*</FONT></TD></TR><TR ALIGN=CENTER>
<TD ALIGN=LEFT COLSPAN=4><A HREF=/NFL/PLAYERS/4/4654/>TORRY HOLT</A></TD>
<TD>7</TD><TD>87</TD><TD>12.4</TD><TD>0</TD><TD>0</TD></TR>
<TR ALIGN=CENTER BGCOLOR=EEEEEE><TD ALIGN=LEFT COLSPAN=4>
<A HREF=/NFL/PLAYERS/2/2914/>ISAAC BRUCE</A></TD><TD>2</TD><TD>34</TD>
<TD>17.0</TD><TD>0</TD><TD>0</TD></TR><TR ALIGN=CENTER>
<TD ALIGN=LEFT COLSPAN=4><A HREF=/NFL/PLAYERS/2/2728/>MARSHALL FAULK</A>
</TD><TD>2</TD><TD>27</TD><TD>13.5</TD><TD>0</TD><TD>0</TD></TR>
<TR ALIGN=CENTER BGCOLOR=EEEEEE><TD ALIGN=LEFT COLSPAN=4>
<A HREF=/NFL/PLAYERS/3/3501/>ERNIE CONWELL</A></TD><TD>1</TD><TD>14</TD>
<TD>14.0</TD><TD>0</TD><TD>0</TD></TR><TR ALIGN=CENTER>
<TD ALIGN=LEFT COLSPAN=4><A HREF=/NFL/PLAYERS/4/4954/>TERRENCE WILKINS</A>
</TD><TD>1</TD><TD>10</TD><TD>10.0</TD><TD>0</TD><TD>0</TD></TR>
<TR ALIGN=CENTER BGCOLOR=EEEEEE><TD ALIGN=LEFT COLSPAN=4>
<A HREF=/NFL/PLAYERS/8/889/>RICKY PROEHL</A></TD><TD>2</TD><TD>9</TD>
<TD>4.5</TD><TD>0</TD><TD>0</TD></TR><TR ALIGN=CENTER>
<TD ALIGN=LEFT COLSPAN=4><A HREF=/NFL/PLAYERS/5/5060/>TRUNG CANIDATE</A></TD>
<TD>1</TD><TD>5</TD><TD>5.0</TD><TD>0</TD><TD>0</TD></TR>
<TR ALIGN=CENTER BGCOLOR=216911><TD ALIGN=LEFT COLSPAN=4>
<FONT FACE=ARIAL SIZE=-2 COLOR=WHITE>
*/

  private void parseTeamAreaLine(String statLine,
				 int numberTableDatas,
				 int scoreType,
				 String team)
  {
    int currentPosition = 0;
    int length = statLine.length();
    boolean done = false;
    int end = 0;
    String name = "";
    int foundDatas = 0;
    int scoreLength = 0;

    String nameAreaString = "/NFL/PLAYERS/";

    while (!done)
    {
      //First get to name
      currentPosition = statLine.indexOf(nameAreaString, currentPosition);
      if (currentPosition == -1)
      {
	done = true;
      }
      else
      {
	currentPosition = statLine.indexOf('>', currentPosition) + 1;
	end = statLine.indexOf(HTML_ANCHOR_END, currentPosition);
	name = statLine.substring(currentPosition, end);
	currentPosition += name.length() + HTML_ANCHOR_END.length();
	foundDatas = 0;
	while (foundDatas < numberTableDatas)
	{
	  currentPosition = statLine.indexOf(HTML_DATA_START, currentPosition) + HTML_DATA_START.length();
	  foundDatas++;
	}
	end = statLine.indexOf(HTML_DATA_END, currentPosition);
	scoreLength = new Integer(statLine.substring(currentPosition, end)).intValue();
	currentPosition = end + HTML_DATA_END.length();

	createAndAddScore(name, team, scoreType, scoreLength);
      }
    }
  }

  public ArrayList getScores()
  {
    ArrayList retval = new ArrayList();
    retval = super.getScores();
    return retval;
  }

  /**
   * There will be a problem if the Jets play the Giants
   */

  private static String getYahooLongName(String orig)
  {
    String retval = orig;

    if (orig.equalsIgnoreCase("NEW YORK JETS"))
    {
      retval = "NEW YORK";
    }
    else if (orig.equalsIgnoreCase("NEW YORK GIANTS"))
    {
      retval = "NEW YORK";
    }

    return retval;
  }

}