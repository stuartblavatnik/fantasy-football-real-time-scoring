package com.twoforboth.realtimescoring.statparser;

import com.twoforboth.realtimescoring.statparser.HTMLConsts;
import com.twoforboth.realtimescoring.data.Score;

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

public class BaseParser implements HTMLConsts
{
  protected final String EXTRA_DEFENSE = " DEFENSE";
  protected final String EXTRA_SPECIAL_TEAMS = " SPECIALS";
  protected final String EXTRA_NONE = "";

  protected String buffer_ = "";                  //String to operate on
  protected int currentCharacterIndex_ = 0;       //Index into the string
  protected int nextCharacterIndex_ = 0;          //Index of the next major section of the buffer (used to keep track of section)

  protected String homeTeam_ = "";
  protected String awayTeam_ = "";

  protected ArrayList scores_ = new ArrayList();   //Store the final scores here
  protected int scoreIndex_ = 0;

  protected StringTokenizer st_ = null;

  //Last team found
  protected String lastTeam_ = "";

  /**
   * Constructor
   * @param homeTeam Full name for home team
   * @param awayTeam Full name for away team
   * @buffer Page to parse
   */

  BaseParser(String homeTeam, String awayTeam, String buffer)
  {
    homeTeam_ = homeTeam.toUpperCase();
    awayTeam_ = awayTeam.toUpperCase();
    buffer_ = buffer.toUpperCase();
  }

  /**
   * Retrieves all characters untila numeric is found
   * @param orig Original string to parse
   * @returns Parsed string
   */

  protected String getName(String orig)
  {
    StringBuffer name = new StringBuffer();

    int i = 0;
    while (orig.charAt(i) < '0' || orig.charAt(i) > '9')
    {
      name.append(orig.charAt(i));
      i++;
      if (i >= orig.length())
      {
	break;
      }
    }
    return name.toString();
  }

  protected String getName(String orig, int startPosition)
  {
    StringBuffer name = new StringBuffer();

    int i = startPosition;
    while (orig.charAt(i) < '0' || orig.charAt(i) > '9')
    {
      name.append(orig.charAt(i));
      i++;
      if (i >= orig.length())
      {
	break;
      }
    }
    return name.toString();
  }

  protected ArrayList getScoresForPlayer(String name, String nflTeam, String nameNoMiddleInitial)
  {
    ArrayList retval = new ArrayList();
    Score score = null;
    int i = 0;
    int size = scores_.size();

    while (i < size)
    {
      score = (Score) scores_.get(i);
/*
      System.out.println("comparing name <" + name +
			 "> to score <" + score.getName() +
			 "> and nflTeam <" + nflTeam +
			 "> with score <" + score.getNFLTeam() + ">");
*/
      if (score.getName().equalsIgnoreCase(name) && score.getNFLTeam().equalsIgnoreCase(nflTeam))
      {
	//System.out.println("Found score type = " + score.getType() + " worth = " + score.getWorth() + " length = " + score.getLength());
	retval.add(score);
      }
      else if (score.getName().equalsIgnoreCase(nameNoMiddleInitial) && score.getNFLTeam().equalsIgnoreCase(nflTeam))
      {
	//System.out.println("Found");
	retval.add(score);
      }
      i++;
    }

    return retval;
  }

  protected ArrayList getScores()
  {
    ArrayList retval = new ArrayList();
    Score score = null;
    int i = 0;
    int size = scores_.size();
/*
    System.out.println("BaseParser getScores() homeTeam = " + homeTeam_ +
		       " awayTeam = " + awayTeam_ + " number of scores = " + size);
*/
    while (i < size)
    {
      score = (Score) scores_.get(i);
      retval.add(score);
      i++;
    }

    return retval;
  }

  /**
   * @returns The next score object or null
   */

  protected Score getNextScore()
  {
    Score retval = null;

    if (scoreIndex_ < scores_.size())
    {
      retval = (Score) scores_.get(scoreIndex_);
      scoreIndex_++;
    }
    return retval;
  }

  protected void createAndAddScore(String playerName,
				   String teamName,
				   int scoreType,
				   int length)
  {
//    System.out.println("BaseParser createAndAddScore() playerName = <" + playerName + "> teamName = <" + teamName + "> type = " + scoreType + " length = " + length);

    Score score = new Score(playerName,
			    teamName,
                            scoreType,
			    length);

    scores_.add(score);
  }

  /**
   * Finds the line containing either team name (look for both individually)
   * Take the smaller one
   * @param currentPosition Current position within the buffer
   * @param followedBy extra string after team
   * @returns Next position containing a team name
   */

  protected int nextTeam(int currentPosition, String followedBy)
  {
    int retval;

    String homeSearch = homeTeam_ + followedBy;
    String awaySearch = awayTeam_ + followedBy;

    int i = buffer_.indexOf(homeSearch, currentPosition);
    int j = buffer_.indexOf(awaySearch, currentPosition);

//    System.out.println("homeTeam <" + homeSearch + "> = " + i + " away <" + awaySearch + "> = " + j );

    if ((i == -1) && (j == -1))
    {
      retval = -1;
      lastTeam_ = "";
    }
    else if (i == -1)
    {
      retval = j;
      lastTeam_ = awayTeam_;
    }
    else if (j == -1)
    {
      retval = i;
      lastTeam_ = homeTeam_;
    }
    else
    {
      retval = (i < j) ? i : j;
      lastTeam_ = (i < j) ? homeTeam_ : awayTeam_;
    }

//    System.out.println("Returning " + retval + " and set lastTeam_ = <" + lastTeam_ + ">");

    return retval;
  }

}