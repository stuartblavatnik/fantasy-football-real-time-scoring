package com.twoforboth.realtimescoring.data;

import com.twoforboth.realtimescoring.data.FantasyTeamMatchup;

import java.util.ArrayList;

/**
 * <p>Title: Scrambled Eggs Football Real Time Scoring</p>
 * <p>Description: Real Time Football Updates</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public class FantasyTeamMatchups
{
  private ArrayList lst_ = new ArrayList();

  public void add(FantasyTeamMatchup ftm)
  {
    lst_.add(ftm);
  }

  public int getSize() { return lst_.size(); }

  public FantasyTeamMatchup getAt(int i)
  {
    return (FantasyTeamMatchup)lst_.get(i);
  }

}