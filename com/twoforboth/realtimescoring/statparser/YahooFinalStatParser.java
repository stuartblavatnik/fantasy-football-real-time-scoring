package com.twoforboth.realtimescoring.statparser;

import com.twoforboth.realtimescoring.data.Score;
import com.twoforboth.realtimescoring.data.ScoringRule;
import com.twoforboth.realtimescoring.data.NFLPlayer;

import com.twoforboth.util.Misc;

import java.util.ArrayList;
import java.util.StringTokenizer;
/**
 * <p>Title: Scrambled Eggs Football Real Time Scoring</p>
 * <p>Description: Real Time Football Updates -- Note, use this one only when the game is determined to be over and
 * make sure you only call it once for that particular game</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public class YahooFinalStatParser extends YahooBaseParser implements StatParser
{
  private final String MAJOR_KEY_SCORING_SUMMARY = "SCORING SUMMARY";
  private final String MAJOR_KEY_LIVE_BOX_SCORE = "LIVE BOX SCORE";
  private final String MAJOR_KEY_TIME_OF_POSSESSION = "TIME OF POSSESSION";

  private final String LIVE_BOX_SACKED_YARDS_LOST = "SACKED-YARDS LOST";
  private final String LIVE_BOX_HAD_INTERCEPTED = "HAD INTERCEPTED";
  private final String LIVE_BOX_FUMBLES_LOST = "FUMBLES-LOST";

  private final String TEAM_AREA_RUSHING = "RUSHING";
  private final String TEAM_AREA_PASSING = "PASSING";
  private final String TEAM_AREA_RECEIVING = "RECEIVING";
  private final String TEAM_AREA_TACKLES = "TACKLES-ASSISTS-SACKS";

//  private final String SCORE_AREA_TOTAL = "TOTAL";

  private final String FOR_SPACE = "FOR ";
  private final String SPACE_YARDS = " YARDS";
/*
  private final String STRING_FIELD_GOAL = "FIELD GOAL";
  private final String STRING_SAFETY = "SAFETY";
  private final String STRING_PASS = "PASS";
  private final String STRING_RUN = "RUN";
  private final String STRING_FUMBLE = "FUMBLE";
  private final String STRING_INTERCEPTION = "INTERCEPTION";
  private final String STRING_KICKOFF = "KICKOFF";
  private final String STRING_PUNT = "PUNT";
  private final String STRING_BLOCK = "BLOCK";
  private final String STRING_KICK = "KICK";
  private final String STRING_TO = "TO";
  private final String FOR_TWO_POINT_CONVERSION = "FOR TWO-POINT CONVERSION";
  private final String FROM_SPACE = "FROM ";
*/
//  private final String TO_SPACE = "TO ";

  String majorKeys_[] = { MAJOR_KEY_SCORING_SUMMARY,
                          MAJOR_KEY_LIVE_BOX_SCORE,
			  MAJOR_KEY_TIME_OF_POSSESSION };

  String liveBoxScoreKeys_[] = { LIVE_BOX_SACKED_YARDS_LOST,
                                 LIVE_BOX_HAD_INTERCEPTED,
				 LIVE_BOX_FUMBLES_LOST };

  String teamAreaKeys_[] = { TEAM_AREA_RUSHING,
                             TEAM_AREA_PASSING,
			     TEAM_AREA_RECEIVING,
			     TEAM_AREA_TACKLES };

  /**
   * Constructor
   * @param homeTeam Full name for home team
   * @param awayTeam Full name for away team
   * @buffer Page to parse
   */

  public YahooFinalStatParser(String homeTeam, String awayTeam, String buffer)
  {
    super(getYahooLongName(homeTeam), getYahooLongName(awayTeam), buffer);
  }

  /**
   * Parses the 4 main sections of the page
   */

  public boolean parse()
  {
    boolean retval = false;
    super.parse();
    if (documentNotFound_ == false)
    {
      retval = parseScoreArea();                 //Parse the top portion for scores
      if (retval)
      {
	retval = parseScoringSummary();            //Parse individual player scores
      }
      if (retval)
      {
	retval = parseLiveBoxScore(majorKeys_, liveBoxScoreKeys_);              //Parse team stats
      }
      if (retval)
      {
	retval = parseTeamArea();                  //Parse individual player yards
      }
    }
    return retval;
  }

  private boolean parseScoreArea()
  {
    boolean retval = true;
    int endIndex = 0;

    String searchHomeTeam = Misc.removeChar(homeTeam_, '.');
    String searchAwayTeam = Misc.removeChar(awayTeam_, '.');

    currentCharacterIndex_ = buffer_.indexOf(SCORE_AREA_TOTAL);
    currentCharacterIndex_ = buffer_.indexOf(searchAwayTeam, currentCharacterIndex_);
    currentCharacterIndex_ = buffer_.indexOf(HTML_BOLD_START, currentCharacterIndex_) + HTML_BOLD_START.length();
    endIndex = buffer_.indexOf(HTML_BOLD_END, currentCharacterIndex_);
    int awayPoints = 0;
    try
    {
      awayPoints = new Integer(buffer_.substring(currentCharacterIndex_, endIndex)).intValue();
    }
    catch (Exception e)
    {
/*
      System.out.println(this.getClass().getName() + " ERROR parseFinalScores() awayTeam_ = <" +
			 searchAwayTeam + "> startIndex = " +
			 currentCharacterIndex_ + " endIndex = " +
			 endIndex + " awayPoints buffer = " +
			 buffer_.substring(currentCharacterIndex_, endIndex));
*/
      retval = false;
    }

//    System.out.println("Looking for homeTeam_ = <" + homeTeam_ + ">");
    currentCharacterIndex_ = buffer_.indexOf(searchHomeTeam, currentCharacterIndex_);
    currentCharacterIndex_ = buffer_.indexOf(HTML_BOLD_START, currentCharacterIndex_) + HTML_BOLD_START.length();
    endIndex = buffer_.indexOf(HTML_BOLD_END, currentCharacterIndex_);
    int homePoints = 0;
    try
    {
      homePoints = new Integer(buffer_.substring(currentCharacterIndex_, endIndex)).intValue();
    }
    catch (Exception ee)
    {
//      System.out.println("ERROR parseFinalScores() homeTeam_ = <" + homeTeam_ + "> startIndex = " + currentCharacterIndex_ + " endIndex = " + endIndex + " homePoints buffer = " + buffer_.substring(currentCharacterIndex_, endIndex));
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

  private boolean parseTeamArea()
  {
    boolean retval = true;
    int start = 0;
    int end = 0;
    StringBuffer workingStringBuffer = new StringBuffer();
    String workingString = "";

    String startWord = "";
    String endWord = "";
    String statLine = "";

    String currentTeam = "";

    ArrayList scoringTypes = new ArrayList();
    scoringTypes.add(new Integer(ScoringRule.PLAYER_WEEKLY_RUSHING_YDS));
    scoringTypes.add(new Integer(ScoringRule.PLAYER_WEEKLY_PASSING_YDS));
    scoringTypes.add(new Integer(ScoringRule.PLAYER_WEEKLY_RECEIVING_YDS));
    scoringTypes.add(new Integer(-1));

    String searchHomeTeam = Misc.removeChar(homeTeam_, '.');
    String searchAwayTeam = Misc.removeChar(awayTeam_, '.');

//    System.out.println("awayTeam_ = " + awayTeam_ + " homeTeam_ = " + homeTeam_);

    int localCharacterIndex = 0;
    //Away then Home
    for (int i = 0; i < 2; i++)
    {
      if (i == 0)
      {
//         currentCharacterIndex_ = buffer_.indexOf(searchHomeTeam, currentCharacterIndex_);
         currentCharacterIndex_ = buffer_.indexOf(searchAwayTeam, currentCharacterIndex_);
         nextCharacterIndex_ = buffer_.indexOf(searchHomeTeam, currentCharacterIndex_);
	 try
	 {
	   workingStringBuffer.append(buffer_.substring(currentCharacterIndex_, nextCharacterIndex_));
	 }
	 catch(Exception pta1)
	 {
/*
	   System.out.println(this.getClass().getName() +
			      " parseTeamArea() A Looking for awayTeam_ = "
			      + searchAwayTeam + " homeTeam_ = " + searchHomeTeam +
			      " currentIndex = " + currentCharacterIndex_ +
			      " nextCharacterIndex = " + nextCharacterIndex_);
*/
	   retval = false;
	 }
	 workingStringBuffer = Misc.stripHTML(workingStringBuffer);
	 workingString = workingStringBuffer.toString();
	 currentTeam = awayTeam_;
      }
      else
      {
	currentCharacterIndex_ = nextCharacterIndex_;
        nextCharacterIndex_ = buffer_.indexOf(teamAreaKeys_[teamAreaKeys_.length - 1], currentCharacterIndex_);
	workingStringBuffer.setLength(0);
	try
	{
	  workingStringBuffer.append(buffer_.substring(currentCharacterIndex_, nextCharacterIndex_ + teamAreaKeys_[teamAreaKeys_.length - 1].length()));
	}
	catch(Exception pta2)
	{
/*
	  System.out.println(this.getClass().getName() +
			     " parseTeamArea() B Looking for key = "
			     + teamAreaKeys_[teamAreaKeys_.length - 1] +
			     " currentIndex = " + currentCharacterIndex_ +
			     " nextCharacterIndex = " + nextCharacterIndex_ +
			     " homeTeam = " + searchHomeTeam +
			     " awayTeam = " + searchAwayTeam);
*/
	   retval = false;
	}
	workingStringBuffer = Misc.stripHTML(workingStringBuffer);
	workingString = workingStringBuffer.toString();
	currentTeam = homeTeam_;
      }

      localCharacterIndex = 0;
      for (int j = 0; j < teamAreaKeys_.length - 1; j++)
      {
	startWord = teamAreaKeys_[j];
	endWord = teamAreaKeys_[j + 1];

	start = workingString.indexOf(startWord, localCharacterIndex) + startWord.length();
	end = workingString.indexOf(endWord, start);
	try
	{
	  statLine = workingString.substring(start, end);
	  createTeamAreaStats(statLine, currentTeam, startWord);
	  localCharacterIndex += statLine.length();
	}
	catch (Exception eTeamAreaKeys)
	{
/*
	  System.out.println(this.getClass().getName() + " parseTeamArea() startWord was " + startWord +
			     " endWord was " + endWord + " start = " + start +
			     " end = " + end +
			     " homeTeam = " + searchHomeTeam +
			     " awayTeam = " + searchAwayTeam +
			     " workingString = " + workingString);
*/
	   retval = false;
	}
      }
      currentCharacterIndex_ += localCharacterIndex;
    }
    return retval;
  }

  /**
   * @param statLine Full line of stats for a particular type
   * @param currentTeam Current NFL Team
   * @param statWord Passing, Rushing or Receiving
   */

  private void createTeamAreaStats(String statLine,
				   String currentTeam,
				   String statWord)
  {
    String element = "";
    String delimiter = "";
    String name = "";
    int yards = 0;
    int begin = 0;
    int end = 0;
    int scoreType = 0;
    /*
        Travis Henry 12-30, Larry Centers 1-1, Drew Bledsoe 1-0.
        Drew Bledsoe 35-49 for 463 yards, 0 INT, 3 TD.
        Peerless Price 13-185, Josh Reed 8-110, Eric Moulds 8-86, Larry Centers 4-44, Jay Riemersma 2-38.
        Moe Williams 17-102, Michael Bennett 10-45, Doug Chapman 4-41, Daunte Culpepper 7-20, Randy Moss 1-5.
        Daunte Culpepper 25-45 for 281 yards, 0 INT, 3 TD. Randy Moss 0-1 for 0 yards, 0 INT, 0 TD.
        Randy Moss 11-111, D'Wayne Bates 3-68, Jim Kleinsasser 6-56, Derrick Alexander 3-25, Chris Walsh 2-21.
    */
/*
    System.out.println("Welcome to createTeamAreaStats() statLine = " + statLine +
		       " currentTeam = " + currentTeam + " statWord = " + statWord);
*/
    if (statWord.equalsIgnoreCase(TEAM_AREA_PASSING))
    {
      delimiter = ".";
      scoreType = ScoringRule.PLAYER_WEEKLY_PASSING_YDS;
    }
    else
    {
      delimiter = ",";
      if (statWord.equalsIgnoreCase(TEAM_AREA_RUSHING))
      {
	scoreType = ScoringRule.PLAYER_WEEKLY_RUSHING_YDS;
      }
      else
      {
	scoreType = ScoringRule.PLAYER_WEEKLY_RECEIVING_YDS;
      }
    }

    //Strtok using , -- last item will have the . at the end
    st_ = new StringTokenizer(statLine, delimiter, false);
    while (st_.hasMoreElements())
    {
      element = st_.nextToken().trim();
      if (delimiter.equals(","))
      {
	if (element.indexOf('.') != -1)
	{
	  element = element.substring(0, element.lastIndexOf('.'));
	}
      }
      name = getName(element);

      if (element.indexOf(FOR_SPACE) != -1)
      {
	//Passing yardage stat
	begin = element.indexOf(FOR_SPACE) + FOR_SPACE.length();
	end = element.indexOf(SPACE_YARDS);
	try
	{
	  yards = new Integer(element.substring(begin, end).trim()).intValue();
	}
	catch (java.lang.NumberFormatException e)
	{
	  yards = 0;
	}
      }
      else
      {
	//Rushing or receiving yardage
	begin = element.indexOf('-') + 1;
	try
	{
	  yards = new Integer(element.substring(begin).trim()).intValue();
	}
	catch (java.lang.NumberFormatException e)      //Look out for MINUS yardage
	{
	  yards = 0;
	}
      }
      createAndAddScore(NFLPlayer.fixName(name.toString()).trim(),
			currentTeam,
                        scoreType,
			yards);

    }
  }


  /**
   * Parses the area in between the text "Scoring Summary" and "Live Box Score"
   */

  private boolean parseScoringSummary()
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
      int index = nextTeam(currentCharacterIndex_, " - ");
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
      //San Diego - Touchdown</b><br>
      //Get the team first (get all characters up till "-" minus 2
      currentNFLTeam = buffer_.substring(currentCharacterIndex_,
					 buffer_.indexOf('-', currentCharacterIndex_) - 1);
      currentCharacterIndex_ += currentNFLTeam.length() + 3;   //Add 3 for " - "

      String scoreTypeText = buffer_.substring(currentCharacterIndex_, buffer_.indexOf('<', currentCharacterIndex_));
      currentCharacterIndex_ += scoreTypeText.length();

      String fullScoreLine = buffer_.substring(currentCharacterIndex_, buffer_.indexOf(':', currentCharacterIndex_));
      currentCharacterIndex_ += fullScoreLine.length();


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
	//createSafetyScore(currentNFLTeam);
	createAndAddScore(currentNFLTeam + EXTRA_DEFENSE,
			  currentNFLTeam,
                          ScoringRule.DEFENSE_WEEKLY_SAFTIES,
			  1);

      }
      else  //Some form of a touchdown
      {
	  //Break up the Touchdown text from the extra point text
	  try
	  {
	    touchdownText =
		fullScoreLine.substring(0, fullScoreLine.indexOf('('));
	    extraPointText =
		fullScoreLine.substring(fullScoreLine.indexOf('(') + 1,
		                        fullScoreLine.indexOf(')'));
	  }
	  catch (java.lang.StringIndexOutOfBoundsException e)
	  {
	    break;
	  }

	  StringBuffer sb = new StringBuffer(touchdownText);
	  sb = Misc.stripHTML(sb);
	  touchdownText = sb.toString();

	  //System.out.println("TOUCHDOWN TEXT = " + touchdownText + " EXTRA POINT TEXT = " + extraPointText);

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
				false);
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
	  else if (touchdownText.indexOf(STRING_LATERAL) != -1)
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
	  if (extraPointText.indexOf(STRING_KICK) != -1)
	  {
	    extraPointText = extraPointText.substring(0, extraPointText.indexOf(STRING_KICK) - 1);
	    parseCreateAddScore(extraPointText,
				currentNFLTeam,
				ScoringRule.PLAYER_WEEKLY_PAT_MADE,
				1,
				EXTRA_NONE,
				false);
	  }
	  else if (extraPointText.indexOf(STRING_TO) != -1)
	  {
	    extraPointText = extraPointText.substring(0, extraPointText.indexOf(FOR_TWO_POINT_CONVERSION) - 1);
	    createTwoPointConversionPass(extraPointText, currentNFLTeam);
	  }
	  else if (extraPointText.indexOf(STRING_RUN) != -1)   //?
	  {
	    extraPointText = extraPointText.substring(0, extraPointText.indexOf(STRING_RUN) - 1);
	    parseCreateAddScore(extraPointText,
				currentNFLTeam,
				ScoringRule.PLAYER_WEEKLY_RUSHING_2PT,
                                1,
				EXTRA_NONE,
				false);
	  }
      }
/*
      System.out.println("currentIndex = " + currentCharacterIndex_ +
			 " nextCharacterIndex_ = " + nextCharacterIndex_);
*/
    }
    return retval;
  }

  private void createTouchdownPassPlayScore(String scoreLine, String currentNFLTeam)
  {
    //PEERLESS PRICE 3 YD PASS FROM DREW BLEDSOE
    String receiverName = getName(scoreLine);
    // Now move to Thrower
    int i = scoreLine.indexOf(FROM_SPACE) + FROM_SPACE.length();
    String throwerName = getName(scoreLine, i);

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

  /**
   * Parses line in the form FirstName LastName Length YD
   */

  private void createFieldGoalScore(String fullScoreLine, String currentNFLTeam)
  {
    String name = getName(fullScoreLine);
    createAndAddScore(NFLPlayer.fixName(name).trim(),
		      currentNFLTeam,
                      ScoringRule.PLAYER_WEEKLY_FG_MADE,
		      1);
  }


  /**
   * @returns The next score object or null
   */

  public Score getNextScore()
  {
    return super.getNextScore();
  }

  /**
   * Converts Quickstats short name to web name.
   * Might put these mappings into a database at some point.
   * @param original shortname (Quickstats format)
   * @returns Yahoo shortname
   */

  public static String getWebName(String orig)
  {
    String retval = orig;

    if (orig.equalsIgnoreCase("SF"))
    {
      retval = "SFO";
    }
    else if (orig.equalsIgnoreCase("NE"))
    {
      retval = "NWE";
    }
    else if (orig.equalsIgnoreCase("GB"))
    {
      retval = "GNB";
    }
    else if (orig.equalsIgnoreCase("SD"))
    {
      retval = "SDG";
    }
    else if (orig.equalsIgnoreCase("KC"))
    {
      retval = "KAN";
    }
    else if (orig.equalsIgnoreCase("NO"))
    {
      retval = "NOR";
    }
    else if (orig.equalsIgnoreCase("TB"))
    {
      retval = "TAM";
    }

    return retval;
  }


  public ArrayList getScoresForPlayer(String name, String nflTeam, String nameNoMiddleInitial)
  {
    ArrayList retval = new ArrayList();
    retval = super.getScoresForPlayer(name, nflTeam, nameNoMiddleInitial);
    return retval;
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
      retval = "NY JETS";
    }
    else if (orig.equalsIgnoreCase("NEW YORK GIANTS"))
    {
      retval = "NY GIANTS";
    }

    return retval;
  }

}