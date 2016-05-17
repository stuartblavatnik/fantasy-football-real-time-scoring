package com.twoforboth.realtimescoring.statparser;

import com.twoforboth.realtimescoring.statparser.BaseParser;
import com.twoforboth.realtimescoring.data.ScoringRule;
import com.twoforboth.realtimescoring.data.NFLPlayer;

/**
 * <p>Title: Scrambled Eggs Football Real Time Scoring</p>
 * <p>Description: Real Time Football Updates</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public class YahooBaseParser extends BaseParser
{
  protected final String LIVE_BOX_SACKED_YARDS_LOST = "SACKED-YARDS LOST";
  protected final String LIVE_BOX_HAD_INTERCEPTED = "HAD INTERCEPTED";
  protected final String LIVE_BOX_FUMBLES_LOST = "FUMBLES-LOST";

  protected final String SCORE_AREA_TOTAL = "TOTAL";

  protected final String STRING_FIELD_GOAL = "FIELD GOAL";
  protected final String STRING_SAFETY = "SAFETY";
  protected final String STRING_PASS = "PASS";
  protected final String STRING_RUN = "RUN";
  protected final String STRING_FUMBLE = "FUMBLE";
  protected final String STRING_INTERCEPTION = "INTERCEPTION";
  protected final String STRING_KICKOFF = "KICKOFF";
  protected final String STRING_PUNT = "PUNT";
  protected final String STRING_BLOCK = "BLOCK";
  protected final String STRING_KICK = "KICK";
  protected final String STRING_TO = "TO";
  protected final String FOR_TWO_POINT_CONVERSION = "FOR TWO-POINT CONVERSION";
  protected final String FROM_SPACE = "FROM ";
  protected final String TO_SPACE = "TO ";
  protected final String STRING_PASS_TO = "PASS TO";
  protected final String STRING_TWO_POINT_CONVERTED = "2PT ATTEMPT CONVERTED, ";
  protected final String STRING_LATERAL = "LATERAL";

  protected final String NEW_YORK_STRING = "NEW YORK";
  protected final String NY_STRING = "NY";

  protected final String DOCUMENT_NOT_FOUND = "DOCUMENT NOT FOUND";

  protected boolean documentNotFound_ = false;

  public YahooBaseParser(String homeTeam, String awayTeam, String buffer)
  {
    super(homeTeam, awayTeam, buffer);
  }

  public boolean parse()
  {
    boolean retval = false;
    if (buffer_.indexOf(DOCUMENT_NOT_FOUND) != -1)
    {
      documentNotFound_ = true;
    }
    else
    {
      scores_.clear();                  //Reset the scores for this parser
      documentNotFound_ = false;
      retval = true;
    }
    return retval;
  }

  /**
   * Parse line in the form<BR>
   * Sacked-yards lost</td><td>3-26</td><td>5-45</td>
   * Had intercepted</td><td>0</td><td>0</td>
   * FUMBLES-LOST</td><td>2-0</td><td>5-3</td>
   * @param statString Full line of stats
   * @key which stat as a string
   */

  protected void createDefesiveScoreLiveBoxScore(String statString, String key)
  {
    boolean found = false;
    int homeStat = -1;
    int awayStat = -1;
    int statType = -1;
    String parsedStatString = "";

    //System.out.println(statString);

    int start = statString.indexOf(HTML_DATA_START) + HTML_DATA_START.length();
    int end = statString.indexOf(HTML_DATA_END, start);

    String temp = statString.substring(start, end);

    if (temp.indexOf('-') != -1)
    {
      if (key.equalsIgnoreCase(LIVE_BOX_FUMBLES_LOST))
      {
	parsedStatString = temp.substring(temp.indexOf('-') + 1);
      }
      else
      {
	parsedStatString = temp.substring(0, temp.indexOf('-'));
      }
    }
    else
    {
      parsedStatString = temp;
    }

    homeStat = new Integer(parsedStatString).intValue();

    start = statString.indexOf(HTML_DATA_START, end) + HTML_DATA_START.length();
    end = statString.indexOf(HTML_DATA_END, start);
    temp = statString.substring(start, end);

    if (temp.indexOf('-') != -1)
    {
      if (key.equalsIgnoreCase(LIVE_BOX_FUMBLES_LOST))
      {
	parsedStatString = temp.substring(temp.indexOf('-') + 1);
      }
      else
      {
	parsedStatString = temp.substring(0, temp.indexOf('-'));
      }
    }
    else
    {
      parsedStatString = temp;
    }

    awayStat = new Integer(parsedStatString).intValue();


    if (key.equalsIgnoreCase(LIVE_BOX_SACKED_YARDS_LOST))
    {
      statType = ScoringRule.DEFENSE_WEEKLY_SACKS;
      found = true;
    }
    else if (key.equalsIgnoreCase(LIVE_BOX_HAD_INTERCEPTED))
    {
      statType = ScoringRule.DEFENSE_WEEKLY_INTERCEPTIONS;
      found = true;
    }
    else if (key.equalsIgnoreCase(LIVE_BOX_FUMBLES_LOST))
    {
      statType = ScoringRule.DEFENSE_WEEKLY_FUMBLES_RECOVERED;
      found = true;
    }

    if (found)
    {
//      System.out.println("home = " + homeStat + " away = " + awayStat);

      //NOTE THESE STATS ARE SUPPOSED TO BE BACKWARDS????????
      createAndAddScore(homeTeam_ + EXTRA_DEFENSE, homeTeam_, statType, homeStat);
      createAndAddScore(awayTeam_ + EXTRA_DEFENSE, awayTeam_, statType, awayStat);
    }
  }


  /**
   * Note -- until I care about length, use 1
   * @param scoreText Full text of score including name and length
   * @param team NFL Team long name
   * @param score type Scoring type constant from ScoringRules class
   * @param length Length of score (usually 1 for this round)
   * @param extra String containing either "DEFENSE or SPECIAL TEAMS"
   * for team stats to add to the name
   * @param parseNameByWord If true then get the name after a word
   */

  protected void parseCreateAddScore(String scoreText,
				   String team,
				   int scoreType,
				   int length,
				   String extra,
				   boolean parseNameByWord)
  {
    //String name = getName(scoreText) + extra;

    int index;
    String name = "";

    if (parseNameByWord)
    {
      String wordToFind = "";
      if (scoreType == ScoringRule.PLAYER_WEEKLY_RUSHING_TDS)
      {
	wordToFind = STRING_RUN;
      }
      else if (scoreType == ScoringRule.PLAYER_WEEKLY_PAT_MADE)
      {
	wordToFind = STRING_KICK;
      }

      name = scoreText.substring(0, scoreText.indexOf(wordToFind)).trim();
    }
    else
    {
      name = getName(scoreText) + extra;
    }
    //Note -- should read in the length -- but for now, don't need it
    createAndAddScore(NFLPlayer.fixName(name).trim(),
		      team,
		      scoreType,
		      length);
  }

  protected void createTwoPointConversionPass(String extraPointText, String currentNFLTeam)
  {
    //FirstName LastName TO FirstName LastName FOR TWO-POINT CONVERSION
    String throwerName = getName(extraPointText);

    // Now move to Thrower
    String toString = TO_SPACE;
    int i = extraPointText.indexOf(toString) + toString.length();
    String receiverName = getName(extraPointText, i);

    //Now create 2 score items (one for thrower and one for receiver)
    createAndAddScore(NFLPlayer.fixName(receiverName).trim(),
		      currentNFLTeam,
		      ScoringRule.PLAYER_WEEKLY_RECEIVING_2PT,
		      1);

    createAndAddScore(NFLPlayer.fixName(throwerName).trim(),
		      currentNFLTeam,
		      ScoringRule.PLAYER_WEEKLY_PASSING_2PT,
		      1);
  }

  protected boolean parseLiveBoxScore(String[] majorKeys, String[] liveBoxScoreKeys)
  {
    boolean retval = true;

    //Move cursor to first key (i.e. Scoring Summary)
    currentCharacterIndex_ = buffer_.indexOf(majorKeys[1]);
    //Set the next buffer to the next major key
    nextCharacterIndex_ = buffer_.indexOf(majorKeys[2]);

    String statString = "";

    for (int i = 0; i < liveBoxScoreKeys.length; i++)
    {
      String currentKey = liveBoxScoreKeys[i];
      currentCharacterIndex_ = buffer_.indexOf(currentKey);
      try
      {
	statString = buffer_.substring(currentCharacterIndex_,
				       buffer_.indexOf(HTML_ROW_END, currentCharacterIndex_));
	createDefesiveScoreLiveBoxScore(statString, liveBoxScoreKeys[i]);
      }
      catch (java.lang.StringIndexOutOfBoundsException e)
      {
	System.out.println("Exception caught looking for " + HTML_ROW_END + " buffer = " + buffer_.substring(currentCharacterIndex_));
	retval = false;
      }
    }
    return retval;
  }
}