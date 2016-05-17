package com.twoforboth.realtimescoring;

import com.twoforboth.realtimescoring.NFLGameChecker;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * <p>Title: Scrambled Eggs Football Real Time Scoring</p>
 * <p>Description: Real Time Football Updates</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public class NFLGameCheckers
{
  private  ArrayList gameCheckers_ = new ArrayList();
  private  ListIterator li_;

  public void add(NFLGameChecker gc)
  {
    gameCheckers_.add(gc);
  }

  public int getSize() { return gameCheckers_.size(); }

  public void setFirst()
  {
    li_ = gameCheckers_.listIterator();
  }

  public NFLGameChecker getNext()
  {
    NFLGameChecker gc = null;

    if (li_.hasNext())
    {
      gc = (NFLGameChecker)li_.next();
    }

    return gc;
  }

  /**
   * Finds a gameChecker object by the longName of one of the participants of the game
   * @param longName Long name of an NFL Team
   * @returns found NFLGameChecker or null
   */

  public NFLGameChecker getGameChecker(String longName)
  {
    NFLGameChecker retval = null;
    NFLGameChecker temp = null;

    ListIterator li = gameCheckers_.listIterator();
    while (li.hasNext())
    {
      temp = (NFLGameChecker)li.next();
      if (temp.getHomeTeamLongName().equalsIgnoreCase(longName) ||
	  temp.getVisitingTeamLongName().equalsIgnoreCase(longName))
      {
	retval = temp;
	break;
      }
    }
    return retval;
  }

}