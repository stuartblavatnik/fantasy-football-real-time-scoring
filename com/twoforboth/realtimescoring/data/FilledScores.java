package com.twoforboth.realtimescoring.data;

import com.twoforboth.realtimescoring.data.FilledScore;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * <p>Title: Filled Scores.java</p>
 * <p>Description: Array of filled score objects</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public class FilledScores
{
  private ArrayList lst_ = new ArrayList();           //Array
  private ListIterator li_ = null;                    //Iterator

  /**
   * Adds a filled score to the list -- Note the object already exists in the
   * array, this method updates it.
   * @param fc Filled Score object
   */
  public void add(FilledScore fc)
  {
    if (!lst_.contains(fc))
    {
      lst_.add(fc);
    }
    else
    {
      lst_.remove(fc);
      lst_.add(fc);
    }
  }

  /**
   * Moves the list iterator to the beginning
   */
  public void first()
  {
    li_ = lst_.listIterator();
  }

  /**
   * Gets the next FilledScore object from the list
   * @returns FilledScore object or null if end of list is reached
   */

  public FilledScore getNext()
  {
    FilledScore fc = null;

    try
    {
      if (li_.hasNext())
      {
	fc = (FilledScore)li_.next();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      fc = null;
    }
    return fc;
  }

  /**
   * Gets the next FilledScore object from the list for a particular nflTeam
   * @returns FilledScore object or null if end of list is reached
   */

  public FilledScore findNextFilledScoreForNFLTeam(String nflTeam)
  {
    FilledScore fc = null;

    while ((fc = getNext()) != null)
    {
      if ( fc.getNFLTeam().equalsIgnoreCase(nflTeam) ||
           fc.getAlternativeNFLTeam().equalsIgnoreCase(nflTeam))
      {
	break;
      }
      else if (fc.getNFLTeam().toUpperCase().indexOf(nflTeam) != -1)
      {
	break;
      }
      else if (fc.getAlternativeNFLTeam().toUpperCase().indexOf(nflTeam) != -1)
      {
	break;
      }
    }
    return fc;
  }

  /**
   * Gets the FilledScore object from the list for a particular player on a
   * particular nflTeam
   * @param name Name of player to find
   * @param nflTeam Name of NFL team
   * @returns FilledScore object or null if not found
   */

  public FilledScore findFilledScore(String name, String nflTeam)
  {
    FilledScore fc = null;

    first();
    while ((fc = getNext()) != null)
    {
      if ((fc.getPlayerName().equalsIgnoreCase(name) ||
	   fc.getPlayerNameNoInitial().equalsIgnoreCase(name)))
      {
	if ( fc.getNFLTeam().equalsIgnoreCase(nflTeam) ||
	     fc.getAlternativeNFLTeam().equalsIgnoreCase(nflTeam))
	{
	  break;
	}
	else if (fc.getNFLTeam().toUpperCase().indexOf(nflTeam) != -1)
	{
	  break;
	}
	else if (fc.getAlternativeNFLTeam().toUpperCase().indexOf(nflTeam) != -1)
	{
	  break;
	}
      }
    }

    return fc;
  }

  /**
   * Zeros out the points for all members of an nflTeam
   * @param nflTeam Name of team
   */

  public void zeroPointsForTeam(String nflTeam)
  {
    FilledScore fc = null;

    first();
    while ((fc = getNext()) != null)
    {
      if (fc.getNFLTeam().equalsIgnoreCase(nflTeam))
      {
	fc.setPoints(0);
      }
      else if (fc.getAlternativeNFLTeam().toUpperCase().indexOf(nflTeam) != -1)
      {
	fc.setPoints(0);
      }
    }
  }
}