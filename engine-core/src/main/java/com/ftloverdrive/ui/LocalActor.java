package com.ftloverdrive.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Pools;
import com.ftloverdrive.core.OverdriveContext;
import com.ftloverdrive.event.local.LocalActorClickedEvent;


/**
 * An actor that issues OVD events in response to libGDX's InputEvents.
 * 
 * TODO: Each actor needs to cache the OverdriveContext in order to be able to
 * enqueue OVD events in response to InputEvents. Maybe have a local manager
 * that translates the events (or just adds the current context to the call),
 * so that the context won't have to be cached?
 *
 */
public abstract class LocalActor extends Actor implements EventListener {

	protected OverdriveContext context;


	public LocalActor( OverdriveContext context ) {
		this.context = context;
	}

	@Override
	public boolean handle( Event event ) {
		if ( event instanceof InputEvent ) {
			InputEvent e = (InputEvent)event;

			if ( e.getType() == InputEvent.Type.touchDown ) {
				LocalActorClickedEvent le = Pools.get( LocalActorClickedEvent.class ).obtain();
				le.init( e );
				context.getScreenEventManager().postDelayedInboundEvent( le );
			}
		}
		return false;
	}
}