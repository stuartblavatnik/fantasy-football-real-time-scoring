package com.twoforboth.realtimescoring.events;

import com.twoforboth.realtimescoring.events.GameStatsEvent;
import com.twoforboth.realtimescoring.events.GameStartedEvent;
import com.twoforboth.realtimescoring.events.GameEndedEvent;
/**
 * <p>Title: Scrambled Eggs Football Real Time Scoring</p>
 * <p>Description: Real Time Football Updates</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public interface GameStatsListener extends java.util.EventListener
{
  void newStatsArrived(GameStatsEvent evt);
  void gameStarted(GameStartedEvent evt);
  void gameEnded(GameEndedEvent evt);
  void gameInitialized(GameInitializedEvent evt);
}