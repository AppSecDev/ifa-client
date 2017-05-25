/**
 * ©  Copyright HCL Technologies Ltd. 2017.  All Rights Reserved.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.ibm.appscan.ifa.client;

import java.util.ArrayList;

public class EventManagerIfa {

	private ArrayList<IProgressIfaListener>m_progress_ifa_listeners=new ArrayList<IProgressIfaListener>();
	private static EventManagerIfa s_manager=new EventManagerIfa();
	private EventManagerIfa() {
	}

	public static void addIfaProgressListener(IProgressIfaListener l){
		s_manager.m_progress_ifa_listeners.add(l);
	}

	public static void removeIfaProgressListener(IProgressIfaListener l){
		s_manager.m_progress_ifa_listeners.remove(l);
	}

	public static void fireProgressChanged(final int progress){
		for (final IProgressIfaListener l:s_manager.m_progress_ifa_listeners){
			l.handleProgress(progress);
		}
	}

	public static void fireProgressTitleChanged(final String current){
		for (final IProgressIfaListener l:s_manager.m_progress_ifa_listeners){
			l.handleProgressTitle(current);
		}
	}

	public static void fireProgressStageChanged(final String current){
		for (final IProgressIfaListener l:s_manager.m_progress_ifa_listeners){
			l.handleProgressStage(current);
		}
	}
	public static void fireCompleted(String completed_url){
		for (final IProgressIfaListener l:s_manager.m_progress_ifa_listeners){
			l.handleComplete(completed_url);
		}
	}
}
