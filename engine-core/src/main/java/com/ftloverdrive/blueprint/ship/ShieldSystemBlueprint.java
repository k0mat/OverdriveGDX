package com.ftloverdrive.blueprint.ship;

import com.badlogic.gdx.utils.Pools;
import com.ftloverdrive.core.OverdriveContext;
import com.ftloverdrive.event.ship.SystemCreationEvent;
import com.ftloverdrive.util.OVDConstants;


public class ShieldSystemBlueprint extends SystemBlueprint {


	public ShieldSystemBlueprint() {
		super( null );
		properties.setString( OVDConstants.BLUEPRINT_NAME, getClass().getSimpleName() );

		properties.setString( OVDConstants.SYSTEM_ICON_NAME, "s-shields" );
		properties.setInt( OVDConstants.LEVEL_MAX, 8 );
		properties.setInt( OVDConstants.POWER_INCREMENT, 2 );
		properties.setInt( OVDConstants.POWER_DISABLED, 1 );
		properties.setInt( OVDConstants.POWER_DESTROYED, 1 );
	}


	@Override
	public int construct( OverdriveContext context ) {
		int systemRefId = context.getNetManager().requestNewRefId();

		SystemCreationEvent createE = Pools.get( SystemCreationEvent.class ).obtain();
		createE.init( systemRefId, getString( OVDConstants.BLUEPRINT_NAME ), properties );
		context.getScreenEventManager().postDelayedEvent( createE );

		return systemRefId;
	}
}
