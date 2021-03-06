package com.ftloverdrive.model.ship;

import com.ftloverdrive.io.ImageSpec;
import com.ftloverdrive.model.OVDModel;


/**
 * Teleportation pad objects which will serve the same function
 * that manually linked door did in the original game (ie. allowing
 * crew to move between normally disconnected rooms)
 * 
 * TODO: Stub
 */
public interface TeleportPadModel extends OVDModel {

	public void setAnimSpec( ImageSpec spec );

	public ImageSpec getAnimSpec();

	public void setConnectedTPadRefId( int tpadRefId );

	public int getConnectedTPadRefId();
}
