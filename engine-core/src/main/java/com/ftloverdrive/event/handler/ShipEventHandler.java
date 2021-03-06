package com.ftloverdrive.event.handler;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.ftloverdrive.core.OverdriveContext;
import com.ftloverdrive.event.OVDEvent;
import com.ftloverdrive.event.OVDEventHandler;
import com.ftloverdrive.event.PropertyEvent;
import com.ftloverdrive.event.player.CrewMoveOrderEvent;
import com.ftloverdrive.event.player.OrderListener;
import com.ftloverdrive.event.ship.ShipCreationEvent;
import com.ftloverdrive.event.ship.ShipCrewAddEvent;
import com.ftloverdrive.event.ship.ShipCrewCreationEvent;
import com.ftloverdrive.event.ship.ShipDoorCreationEvent;
import com.ftloverdrive.event.ship.ShipLayoutConnectTeleportPadsEvent;
import com.ftloverdrive.event.ship.ShipLayoutCrewPlacementEvent;
import com.ftloverdrive.event.ship.ShipLayoutDoorAddEvent;
import com.ftloverdrive.event.ship.ShipLayoutListener;
import com.ftloverdrive.event.ship.ShipLayoutRoomAddEvent;
import com.ftloverdrive.event.ship.ShipLayoutSystemIconAddEvent;
import com.ftloverdrive.event.ship.ShipLayoutTeleportPadAddEvent;
import com.ftloverdrive.event.ship.ShipPropertyEvent;
import com.ftloverdrive.event.ship.ShipPropertyListener;
import com.ftloverdrive.event.ship.ShipPropertySentinel;
import com.ftloverdrive.event.ship.ShipRoomCreationEvent;
import com.ftloverdrive.event.ship.ShipRoomImageChangeEvent;
import com.ftloverdrive.event.ship.ShipSystemAddEvent;
import com.ftloverdrive.event.ship.ShipTeleportPadCreationEvent;
import com.ftloverdrive.event.ship.SystemCreationEvent;
import com.ftloverdrive.io.ImageSpec;
import com.ftloverdrive.model.ship.CrewModel;
import com.ftloverdrive.model.ship.DefaultCrewModel;
import com.ftloverdrive.model.ship.DefaultDoorModel;
import com.ftloverdrive.model.ship.DefaultRoomModel;
import com.ftloverdrive.model.ship.DefaultTeleportPadModel;
import com.ftloverdrive.model.ship.DoorModel;
import com.ftloverdrive.model.ship.RoomModel;
import com.ftloverdrive.model.ship.ShipCoordinate;
import com.ftloverdrive.model.ship.ShipModel;
import com.ftloverdrive.model.ship.TeleportPadModel;
import com.ftloverdrive.model.ship.TestShipModel;
import com.ftloverdrive.model.system.SystemModel;


public class ShipEventHandler implements OVDEventHandler {

	private Class[] eventClasses;
	private Class[] listenerClasses;


	public ShipEventHandler() {
		eventClasses = new Class[] {
				ShipCreationEvent.class,
				ShipPropertyEvent.class,
				ShipLayoutRoomAddEvent.class,
				ShipRoomCreationEvent.class,
				ShipRoomImageChangeEvent.class,
				ShipLayoutDoorAddEvent.class,
				ShipDoorCreationEvent.class,
				ShipLayoutTeleportPadAddEvent.class,
				ShipLayoutConnectTeleportPadsEvent.class,
				ShipTeleportPadCreationEvent.class,
				ShipCrewCreationEvent.class,
				ShipCrewAddEvent.class,
				ShipLayoutCrewPlacementEvent.class,
				CrewMoveOrderEvent.class,
				ShipLayoutSystemIconAddEvent.class,
				SystemCreationEvent.class,
				ShipSystemAddEvent.class
		};
		listenerClasses = new Class[] {
				ShipPropertyListener.class,
				ShipPropertySentinel.class,
				ShipLayoutListener.class,
				OrderListener.class
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

		// Ship related

		if ( e instanceof ShipCreationEvent ) {
			ShipCreationEvent event = (ShipCreationEvent)e;

			int shipRefId = event.getShipRefId();
			ShipModel shipModel = context.getBlueprintManager().createModel( event.getShipBlueprintId() );
			if ( shipModel == null ) {
				shipModel = new TestShipModel(); // TODO: Change to default ship model
			}
			shipModel.getProperties().setAll( event.getProperties() );

			context.getReferenceManager().addObject( shipModel, shipRefId );
		}
		else if ( e instanceof ShipPropertyEvent ) {
			ShipPropertyEvent event = (ShipPropertyEvent)e;

			for ( int i = listeners.length - 2; i >= 0; i -= 2 ) {
				if ( listeners[i] == ShipPropertySentinel.class )
					( (ShipPropertySentinel)listeners[i + 1] ).shipPropertyChanging( context, event );
			}

			int shipRefId = event.getModelRefId();
			ShipModel shipModel = context.getReferenceManager().getObject( shipRefId, ShipModel.class );
			if ( event.getPropertyType() == PropertyEvent.INT_TYPE ) {
				if ( event.getAction() == PropertyEvent.SET_ACTION ) {
					int value = event.getIntValue();
					String key = event.getPropertyKey();
					shipModel.getProperties().setInt( key, value );
				}
				else if ( event.getAction() == PropertyEvent.INCREMENT_ACTION ) {
					int value = event.getIntValue();
					String key = event.getPropertyKey();
					shipModel.getProperties().incrementInt( key, value );
				}
			}

			for ( int i = listeners.length - 2; i >= 0; i -= 2 ) {
				if ( listeners[i] == ShipPropertyListener.class )
					( (ShipPropertyListener)listeners[i + 1] ).shipPropertyChanged( context, event );
			}
		}

		// ==============================================================================================
		// Room related

		else if ( e instanceof ShipRoomCreationEvent ) {
			ShipRoomCreationEvent event = (ShipRoomCreationEvent)e;

			int roomRefId = event.getRoomRefId();
			RoomModel roomModel = new DefaultRoomModel();
			context.getReferenceManager().addObject( roomModel, roomRefId );
		}
		else if ( e instanceof ShipRoomImageChangeEvent ) {
			ShipRoomImageChangeEvent event = (ShipRoomImageChangeEvent)e;

			if ( event.getEventType() == ShipRoomImageChangeEvent.DECOR ) {
				int roomRefId = event.getRoomRefId();
				ImageSpec imageSpec = event.getImageSpec();
				RoomModel roomModel = context.getReferenceManager().getObject( roomRefId, RoomModel.class );

				roomModel.setRoomDecorImageSpec( imageSpec );
			}
		}
		else if ( e instanceof ShipLayoutRoomAddEvent ) {
			ShipLayoutRoomAddEvent event = (ShipLayoutRoomAddEvent)e;

			int shipRefId = event.getShipRefId();
			ShipModel shipModel = context.getReferenceManager().getObject( shipRefId, ShipModel.class );
			int roomRefId = event.getRoomRefId();
			ShipCoordinate[] roomCoords = event.getRoomCoords();

			shipModel.getLayout().addRoom( roomRefId, roomCoords );

			ShipCoordinate defaultIconCoord = roomCoords[0];
			shipModel.getLayout().setRoomSystemIcon( roomRefId, new Vector2( defaultIconCoord.x, defaultIconCoord.y ) );

			for ( int i = listeners.length - 2; i >= 0; i -= 2 ) {
				if ( listeners[i] == ShipLayoutListener.class )
					( (ShipLayoutListener)listeners[i + 1] ).shipLayoutRoomAdded( context, event );
			}
		}

		// ==============================================================================================
		// Door related

		else if ( e instanceof ShipDoorCreationEvent ) {
			ShipDoorCreationEvent event = (ShipDoorCreationEvent)e;

			int doorRefId = event.getDoorRefId();
			DoorModel doorModel = new DefaultDoorModel();
			context.getReferenceManager().addObject( doorModel, doorRefId );
		}
		else if ( e instanceof ShipLayoutDoorAddEvent ) {
			ShipLayoutDoorAddEvent event = (ShipLayoutDoorAddEvent)e;

			int shipRefId = event.getShipRefId();
			ShipModel shipModel = context.getReferenceManager().getObject( shipRefId, ShipModel.class );
			int doorRefId = event.getDoorRefId();

			shipModel.getLayout().addDoor( doorRefId, event.getDoorCoords() );

			for ( int i = listeners.length - 2; i >= 0; i -= 2 ) {
				if ( listeners[i] == ShipLayoutListener.class )
					( (ShipLayoutListener)listeners[i + 1] ).shipLayoutDoorAdded( context, event );
			}
		}

		// ==============================================================================================
		// Teleport pad related

		else if ( e instanceof ShipTeleportPadCreationEvent ) {
			ShipTeleportPadCreationEvent event = (ShipTeleportPadCreationEvent)e;

			int tpadRefId = event.getTeleportPadRefId();
			TeleportPadModel tpadModel = new DefaultTeleportPadModel();
			context.getReferenceManager().addObject( tpadModel, tpadRefId );
		}
		else if ( e instanceof ShipLayoutTeleportPadAddEvent ) {
			ShipLayoutTeleportPadAddEvent event = (ShipLayoutTeleportPadAddEvent)e;

			int shipRefId = event.getShipRefId();
			ShipModel shipModel = context.getReferenceManager().getObject( shipRefId, ShipModel.class );
			int tpadRefId = event.getTeleportPadRefId();

			shipModel.getLayout().addTeleportPad( tpadRefId, event.getTeleportPadCoords() );

			for ( int i = listeners.length - 2; i >= 0; i -= 2 ) {
				if ( listeners[i] == ShipLayoutListener.class )
					( (ShipLayoutListener)listeners[i + 1] ).shipLayoutTeleportPadAdded( context, event );
			}
		}
		else if ( e instanceof ShipLayoutConnectTeleportPadsEvent ) {
			ShipLayoutConnectTeleportPadsEvent event = (ShipLayoutConnectTeleportPadsEvent)e;

			int tpadRefId = event.getTeleportPadRefId();
			TeleportPadModel tpadModel = context.getReferenceManager().getObject( tpadRefId, TeleportPadModel.class );
			tpadModel.setConnectedTPadRefId( event.getTargetTeleportPadRefId() );
		}

		// ==============================================================================================
		// System related

		else if ( e instanceof ShipLayoutSystemIconAddEvent ) {
			ShipLayoutSystemIconAddEvent event = (ShipLayoutSystemIconAddEvent)e;

			int shipRefId = event.getShipRefId();
			ShipModel shipModel = context.getReferenceManager().getObject( shipRefId, ShipModel.class );
			shipModel.getLayout().setRoomSystemIcon( event.getRoomRefId(), event.getIconOffset() );
		}
		else if ( e instanceof SystemCreationEvent ) {
			SystemCreationEvent event = (SystemCreationEvent)e;

			int systemRefId = event.getSystemRefId();
			SystemModel sysModel = context.getBlueprintManager().createModel( event.getSystemBlueprintId() );
			if ( sysModel == null )
				throw new RuntimeException( "System blueprint doesn't have an associated model: " + event.getSystemBlueprintId() );
			sysModel.getProperties().setAll( event.getProperties() );

			context.getReferenceManager().addObject( sysModel, systemRefId );
		}
		else if ( e instanceof ShipSystemAddEvent ) {
			ShipSystemAddEvent event = (ShipSystemAddEvent)e;

			int shipRefId = event.getShipRefId();
			ShipModel shipModel = context.getReferenceManager().getObject( shipRefId, ShipModel.class );
			shipModel.getLayout().setSystemRoom( event.getSystemRefId(), event.getRoomRefId() );
		}

		// ==============================================================================================
		// Crew related

		else if ( e instanceof ShipCrewCreationEvent ) {
			ShipCrewCreationEvent event = (ShipCrewCreationEvent)e;

			int crewRefId = event.getCrewRefId();
			CrewModel crewModel = new DefaultCrewModel();
			context.getReferenceManager().addObject( crewModel, crewRefId );
		}
		else if ( e instanceof ShipCrewAddEvent ) {
			ShipCrewAddEvent event = (ShipCrewAddEvent)e;

			int shipRefId = event.getShipRefId();
			ShipModel shipModel = context.getReferenceManager().getObject( shipRefId, ShipModel.class );

			shipModel.addCrew( event.getCrewRefId() );
		}
		else if ( e instanceof ShipLayoutCrewPlacementEvent ) {
			ShipLayoutCrewPlacementEvent event = (ShipLayoutCrewPlacementEvent)e;

			int shipRefId = event.getShipRefId();
			ShipModel shipModel = context.getReferenceManager().getObject( shipRefId, ShipModel.class );
			int crewRefId = event.getCrewRefId();

			shipModel.getLayout().placeCrew( crewRefId, event.getCrewCoords() );
		}
		else if ( e instanceof CrewMoveOrderEvent ) {
			CrewMoveOrderEvent event = (CrewMoveOrderEvent)e;

			for ( int i = listeners.length - 2; i >= 0; i -= 2 ) {
				if ( listeners[i] == OrderListener.class ) {
					( (OrderListener)listeners[i + 1] ).orderIssued( context, event );
				}
			}
		}
	}

	@Override
	public void disposeEvent( OVDEvent e ) {
		if ( e.getClass() == ShipCreationEvent.class ) {
			Pools.get( ShipCreationEvent.class ).free( (ShipCreationEvent)e );
		}
		else if ( e.getClass() == ShipPropertyEvent.class ) {
			Pools.get( ShipPropertyEvent.class ).free( (ShipPropertyEvent)e );
		}
		else if ( e.getClass() == ShipRoomCreationEvent.class ) {
			Pools.get( ShipRoomCreationEvent.class ).free( (ShipRoomCreationEvent)e );
		}
		else if ( e.getClass() == ShipRoomImageChangeEvent.class ) {
			Pools.get( ShipRoomImageChangeEvent.class ).free( (ShipRoomImageChangeEvent)e );
		}
		else if ( e.getClass() == ShipLayoutRoomAddEvent.class ) {
			Pools.get( ShipLayoutRoomAddEvent.class ).free( (ShipLayoutRoomAddEvent)e );
		}
		else if ( e.getClass() == ShipDoorCreationEvent.class ) {
			Pools.get( ShipDoorCreationEvent.class ).free( (ShipDoorCreationEvent)e );
		}
		else if ( e.getClass() == ShipLayoutDoorAddEvent.class ) {
			Pools.get( ShipLayoutDoorAddEvent.class ).free( (ShipLayoutDoorAddEvent)e );
		}
		else if ( e.getClass() == ShipTeleportPadCreationEvent.class ) {
			Pools.get( ShipTeleportPadCreationEvent.class ).free( (ShipTeleportPadCreationEvent)e );
		}
		else if ( e.getClass() == ShipLayoutTeleportPadAddEvent.class ) {
			Pools.get( ShipLayoutTeleportPadAddEvent.class ).free( (ShipLayoutTeleportPadAddEvent)e );
		}
		else if ( e.getClass() == ShipLayoutConnectTeleportPadsEvent.class ) {
			Pools.get( ShipLayoutConnectTeleportPadsEvent.class ).free( (ShipLayoutConnectTeleportPadsEvent)e );
		}
		else if ( e.getClass() == ShipCrewCreationEvent.class ) {
			Pools.get( ShipCrewCreationEvent.class ).free( (ShipCrewCreationEvent)e );
		}
		else if ( e.getClass() == ShipCrewAddEvent.class ) {
			Pools.get( ShipCrewAddEvent.class ).free( (ShipCrewAddEvent)e );
		}
		else if ( e.getClass() == ShipLayoutCrewPlacementEvent.class ) {
			Pools.get( ShipLayoutCrewPlacementEvent.class ).free( (ShipLayoutCrewPlacementEvent)e );
		}
		else if ( e.getClass() == CrewMoveOrderEvent.class ) {
			Pools.get( CrewMoveOrderEvent.class ).free( (CrewMoveOrderEvent)e );
		}
	}
}
