package games.stendhal.client.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import games.stendhal.common.Direction;

import java.awt.geom.Rectangle2D;

import marauroa.common.game.RPObject;
import marauroa.common.game.RPObject.ID;

import org.junit.Ignore;
import org.junit.Test;

public class EntityTest {

	@Test
	public final void testEntity() {
		Entity en = new MockEntity();
		
		assertEquals(0.0,en.getX());
		assertEquals(0.0,en.getY());
		assertEquals(0.0,en.dx);
		assertEquals(0.0,en.dy);

		
	}

	
	@Test // throws ounnoticed Attribute not found exception
	public final void testEntityInvalidRPObject() {
		Entity en = EntityFabric.createEntity(new RPObject());
		assertEquals(null, en);
	}
	@Test
	public final void testEntityRPObject() {
		RPObject rpo = new RPObject();
		rpo.put("type", "hugo");
		Entity en = new MockEntity();
		en.init(rpo);
		assertEquals("hugo", en.getType());
		assertEquals("hugo", en.getName());

	}

	@Test
	public final void testGet_IDToken() {
		Entity en = new MockEntity();
		assertNotNull(en.ID_Token);

	}

	@Test
	public final void testGetID() {
		
		RPObject rpo = new RPObject();
		rpo.put("type", "hugo");
		rpo.setID(new ID(1, "woohoo"));
		Entity en = new MockEntity();
		en.init(rpo);
		assertNotNull("id must not be null",en.getID());
		assertEquals(1, en.getID().getObjectID());
		assertEquals("woohoo", en.getID().getZoneID());
	}

	@Test
	public final void testGetNamegetType() {
		Entity en;
		RPObject rpo;
		rpo = new RPObject();
		rpo.put("type", "_hugo");
		en = new MockEntity();
		en.init(rpo);
		assertEquals("_hugo", en.getType());
		assertEquals(" hugo", en.getName());
		rpo = new RPObject();
		rpo.put("type", "hugo");
		rpo.put("name", "ragnarok");
		en = new MockEntity();
		en.init(rpo);
		assertEquals("hugo", en.getType());
		assertEquals("ragnarok", en.getName());
	}

	@Test
	public final void testGetXGetY() {
		Entity en;
		en = new MockEntity();

		assertEquals(0.0, en.getX());
		assertEquals(0.0, en.getY());
		en.onMove(3, 4, Direction.STOP, 0);
		assertEquals(3.0, en.getX());
		assertEquals(4.0, en.getY());

	}



	

	@Test
	public final void testDistance() {
		
		User to = new User();
		Entity en = new MockEntity();
		en.onMove(3, 4, Direction.STOP, 0);
		assertEquals(3.0, en.getX());
		assertEquals(4.0, en.getY());
		assertEquals(25.0, en.distance(to));
		assertEquals(0.0, to.distance(to));
	}

	@Test
	
	public final void testTranslate() {
		assertEquals("data/sprites/hugo.png", Entity.translate("hugo"));
		assertEquals("data/sprites/fire.png", Entity.translate("fire"));
		
	}

	@Test
	public final void testGetSprite() {
		Entity en;
		RPObject rpo;
		rpo = new RPObject();
		rpo.put("type", "_hugo");

		en = new MockEntity();
		en.init(rpo);
		
		assertNotNull(en.getSprite());

	}

	@Test
	public final void testSetAudibleRangegetAudibleArea() {
		Entity en;
		en = new MockEntity();
		assertNull(en.getAudibleArea());
		en.setAudibleRange(5d);
		Rectangle2D rectangle = new Rectangle2D.Double(-5d, -5d, 10d, 10d);
		assertEquals(rectangle, en.getAudibleArea());
		en.setAudibleRange(1d);
		rectangle = new Rectangle2D.Double(-1d, -1d, 2d, 2d);
		assertEquals(rectangle, en.getAudibleArea());

	}

	@Test
	@Ignore
	public final void testLoadSprite() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testCalcDeltaMovement() {
		assertEquals(1.0,Entity.calcDeltaMovement(1, 1, 1));
		assertEquals(0.5,Entity.calcDeltaMovement(1, 1, 0.5));
		assertEquals(0.0,Entity.calcDeltaMovement(2, 1, 0.5));
		assertEquals(1.0,Entity.calcDeltaMovement(1, 2, 0.5));
		assertEquals(0.1,Entity.calcDeltaMovement(1, 1, 0.1));
		assertEquals(-0.4,Entity.calcDeltaMovement(2, 1, 0.1));
		assertEquals(1.1,Entity.calcDeltaMovement(1, 3, 0.1));

		assertEquals(0.5,Entity.calcDeltaMovement(2, 1, 1));
		assertEquals(1.5,Entity.calcDeltaMovement(1, 2, 1));
			
	}

	@Test

	public final void testOnMove() {
//		TODO: try to find out why this behaves as weird as it does astridemma
		Entity en = new MockEntity();
		en.onMove(1,2, Direction.DOWN, 1);
		assertEquals(1.0,en.getX());
		assertEquals(2.0,en.getY());
		assertEquals(0.0,en.dx);
		assertEquals(1.0,en.dy);
		
		
 		
	}

	@Test
	public final void testOnStop() {
		Entity en;
		RPObject rpo;
		rpo = new RPObject();
		rpo.put("type", "_hugo");

		en = new MockEntity();
		en.init(rpo);
		assertTrue(en.stopped());
		en.onStop(0, 0);
		assertTrue(en.stopped());
	}

	@Test
	@Ignore
	public final void testOnEnter() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testOnLeave() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testOnEnterZone() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testOnLeaveZone() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testOnAdded() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testOnChangedAdded() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testOnChangedRemoved() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testOnRemoved() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testOnCollideWith() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testOnCollide() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testDraw() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testMove() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testStopped() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testPlaySoundStringIntIntInt() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testPlaySoundStringIntInt() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testGetNumSlots() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testGetSlot() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testGetSlots() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testGetModificationCount() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testIsModified() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testGetArea() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testGetDrawedArea() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	
	public final void testDefaultAction() {
		Entity en = new MockEntity();
		assertEquals(ActionType.LOOK, en.defaultAction());

	}

	@Test
	public final void testOfferedActions() {
		Entity en = new MockEntity();
		String[] str = new String[1];
		str[0]="Look";
		assertEquals(str, en.offeredActions());
	}

	@Test
	@Ignore
	public final void testBuildOfferedActions() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testOnAction() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testCompareTo() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	@Ignore
	public final void testGetZIndex() {
		fail("Not yet implemented"); // TODO
	}

	private class MockEntity extends Entity {

	
		public MockEntity() {
		}

		@Override
		public Rectangle2D getArea() {
			return null;
		}

		@Override
		public Rectangle2D getDrawedArea() {
			return null;
		}

		@Override
		public int getZIndex() {
			return 0;
		}

		@Override
		protected Entity2DView createView() {
			return null;
		}
	}
}
