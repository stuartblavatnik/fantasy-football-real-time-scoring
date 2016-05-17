package com.twoforboth.realtimescoring.data;

import com.twoforboth.realtimescoring.data.ScoringRule;
import com.twoforboth.communication.ServletInfo;
import com.twoforboth.communication.Servlet;
import com.twoforboth.communication.CantConnectException;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.StringTokenizer;

/**
 * <p>Title: Scrambled Eggs Football Real Time Scoring</p>
 * <p>Description: Real Time Football Updates</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public class ScoringRules
{
  private ArrayList lst_ = new ArrayList();
  private StringTokenizer st_ = null;
  private StringTokenizer ist_ = null;
  private String leagueYear_ = "";
  private String leagueName_ = "";

  private  Servlet getScoringRulesServlet_ =
      new Servlet(ServletInfo.GET_SCORE_RULES_SERVLET_NAME,
		  ServletInfo.GET_SCORE_RULES_SERVLET_PARAMETERS);

  /**
   * Constructor
   * @param leagueName Name of league
   * @param leagueYear Year of league
   */

  public ScoringRules(String leagueName, String leagueYear)
  {
    leagueName_ = leagueName;
    leagueYear_ = leagueYear;
    //Get the rules from the database and create objects for each rule
    fillScoringRules(getScoringRules());
  }

  /**
   * Calculates the score based on length of stat, type of stat and position played
   * @param value Length of stat
   * @param type of stat
   * @param position position played
   * @returns worth
   */

  public int doScore(int value, int id, String position)
  {
    int retval = 0;

    //ScoringRule scoringRule = getScoringRule(id, position);
    ArrayList scoringRules = getScoringRules(id, position);

    for (int i = 0; i < scoringRules.size(); i++)
    {
      retval = ((ScoringRule)scoringRules.get(i)).doScore(value);
      if (retval != 0)
      {
	break;
      }
    }

/*
    if (scoringRule != null)
    {
      retval = scoringRule.doScore(value);
    }
*/
    return retval;
  }

  /**
   * Gets the scoring rules for a particular type and position
   * @param id Type of score
   * @param position Position played
   * @returns ArrayList of rules
   */

  private ArrayList getScoringRules(int id, String position)
  {
    ScoringRule scoringRule = null;
//    ScoringRule retval = null;
    ArrayList retval = new ArrayList();
    ListIterator li = lst_.listIterator();
    while (li.hasNext())
    {
      scoringRule = (ScoringRule)li.next();
      if (scoringRule.getID() == id &&
	  scoringRule.getPosition().equalsIgnoreCase(position))
      {
//	retval = scoringRule;
	retval.add(scoringRule);
//	break;
      }
    }
    return retval;
  }

  /**
   * Creates scoring rule objects from a delimited string and adds
   * the objects to an internal array
   * @param scoringRules Delimited string
   */

  private  void fillScoringRules(String scoringRules)
  {
    //scoringRulesList_
    int id;
    int type;
    int worth;
    int minVal;
    int maxVal;
    int rate;
    String position;
    String innerString = "";

    st_ = new StringTokenizer(scoringRules, ServletInfo.DELIMITER, false);
    while (st_.hasMoreElements())
    {
      ist_ = new StringTokenizer(st_.nextToken(), ",", false);

      id = new Integer(ist_.nextToken()).intValue();
      type = new Integer(ist_.nextToken()).intValue();
      worth = new Integer(ist_.nextToken()).intValue();
      minVal = new Integer(ist_.nextToken()).intValue();
      maxVal = new Integer(ist_.nextToken()).intValue();
      rate = new Integer(ist_.nextToken()).intValue();
      position = ist_.nextToken();

      ScoringRule sr = new ScoringRule(id,
				       type,
				       worth,
				       minVal,
				       maxVal,
				       rate,
				       position);
      lst_.add(sr);
    }
  }

  /**
   * Retrieves the scoring rules from the database using a servlet
   * @returns String in a delimited format
   */

  private String getScoringRules()
  {
    String retval = "";
    StringBuffer sb = new StringBuffer();

    sb.append(leagueYear_);
    sb.append(ServletInfo.DELIMITER);
    sb.append(leagueName_);

    try
    {
      retval = getScoringRulesServlet_.execute(sb.toString());
    }
    catch (CantConnectException cce)
    {
      retval = "";
    }

    return retval;
  }
}