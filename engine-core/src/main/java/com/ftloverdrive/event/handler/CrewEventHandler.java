package com.ftloverdrive.event.handler;

import com.badlogic.gdx.utils.Pools;
import com.ftloverdrive.core.OverdriveContext;
import com.ftloverdrive.event.AbstractPropertyEvent;
import com.ftloverdrive.event.OVDEvent;
import com.ftloverdrive.event.OVDEventHandler;
import com.ftloverdrive.event.ship.CrewPropertyEvent;
import com.ftloverdrive.event.ship.CrewPropertyListener;
import com.ftloverdrive.model.ship.CrewModel;

public class CrewEventHandler implements OVDEventHandler {
	private Class[] eventClasses;
	private Class[] listenerClasses;

	public CrewEventHandler() {
		eventClasses = new Class[] {
			CrewPropertyEvent.class
		};
		listenerClasses = new Class[] {
			CrewPropertyListener.class,
		};
	}

	@Override
	public Class[] getEventClasses() {
		return eventClasses;
	}

	@Override
	public Class[] getListenerClasses() {
		return listenerClasses;
	}

	@Override
	public void handle( OverdriveContext context, OVDEvent e, Object[] listeners ) {
		if ( e instanceof CrewPropertyEvent ) {
			CrewPropertyEvent event = (CrewPropertyEvent)e;

			int crewRefId = event.getCrewRefId();
			CrewModel crewModel = context.getReferenceManager().getObject( crewRefId, CrewModel.class );
			if ( event.getPropertyType() == AbstractPropertyEvent.BOOL_TYPE ) {
				if ( event.getAction() == AbstractPropertyEvent.SET_ACTION ) {
					boolean value = event.getBoolValue();
					String key = event.getPropertyKey();
					crewModel.getProperties().setBool( key, value );
				}
				else if ( event.getAction() == AbstractPropertyEvent.TOGGLE_ACTION ) {
					String key = event.getPropertyKey();
					crewModel.getProperties().toggleBool( key );
				}
			}
			else if ( event.getPropertyType() == AbstractPropertyEvent.INT_TYPE ) {
				if ( event.getAction() == AbstractPropertyEvent.SET_ACTION ) {
					int value = event.getIntValue();
					String key = event.getPropertyKey();
					crewModel.getProperties().setInt( key, value );
				}
				else if ( event.getAction() == AbstractPropertyEvent.INCREMENT_ACTION ) {
					int value = event.getIntValue();
					String key = event.getPropertyKey();
					crewModel.getProperties().incrementInt( key, value );
				}
			}

			for ( int i = listeners.length-2; i >= 0; i-=2 ) {
				if ( listeners[i] == CrewPropertyListener.class ) {
					((CrewPropertyListener)listeners[i+1]).crewPropertyChanged( context, event );
				}
			}
		}
	}

	@Override
	public void disposeEvent( OVDEvent e ) {
		if ( e.getClass() == CrewPropertyEvent.class ) {
			Pools.get( CrewPropertyEvent.class ).free( (CrewPropertyEvent)e );
		}
	}
}
