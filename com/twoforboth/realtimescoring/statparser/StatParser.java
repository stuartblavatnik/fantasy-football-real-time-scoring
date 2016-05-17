package com.twoforboth.realtimescoring.statparser;

import com.twoforboth.realtimescoring.data.Score;
import java.util.ArrayList;
/**
 * <p>Title: Scrambled Eggs Football Real Time Scoring</p>
 * <p>Description: Real Time Football Updates</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public interface StatParser
{
  public boolean parse();
  public Score getNextScore();
  public ArrayList getScoresForPlayer(String name, String nflTeam, String nameNoMiddleInitial);
  public ArrayList getScores();
}