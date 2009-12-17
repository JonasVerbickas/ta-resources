package games.stendhal.server.maps.kirdneh.city;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.server.game.db.DatabaseFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import utilities.QuestHelper;
import utilities.ZonePlayerAndNPCTestImpl;

/**
 * Test buying roses.
 * @author Martin Fuchs
 */
public class FlowerSellerNPCTest extends ZonePlayerAndNPCTestImpl {

	private static final String ZONE_NAME = "int_ados_felinas_house";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		new DatabaseFactory().initializeDatabase();
		MockStendlRPWorld.get();

		QuestHelper.setUpBeforeClass();

		setupZone(ZONE_NAME, new FlowerSellerNPC());
	}


	public FlowerSellerNPCTest() {
		super(ZONE_NAME, "Fleur");
	}

	@Test
	public void testHiAndBye() {
		final SpeakerNPC npc = getNPC("Fleur");
		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi Fleur"));
		assertEquals("Hi! Are you here to #trade?", getReply(npc));

		assertTrue(en.step(player, "bye"));
		assertEquals("Come back soon!", getReply(npc));
	}

	@Test
	public void testBuyFlower() {
		final SpeakerNPC npc = getNPC("Fleur");
		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi"));
		assertEquals("Hi! Are you here to #trade?", getReply(npc));

		assertTrue(en.step(player, "job"));
		assertEquals("I sell roses in this here market.", getReply(npc));

		assertTrue(en.step(player, "trade"));
		assertEquals("I sell rose.", getReply(npc));

		// There is currently no quest response defined for Fleur.
		assertFalse(en.step(player, "quest"));

		assertTrue(en.step(player, "buy"));
		assertEquals("Please tell me what you want to buy.", getReply(npc));

		assertTrue(en.step(player, "buy dog"));
		assertEquals("Sorry, I don't sell dogs.", getReply(npc));

		assertTrue(en.step(player, "buy candle"));
		assertEquals("Sorry, I don't sell candles.", getReply(npc));

		assertTrue(en.step(player, "buy a glass of wine"));
		assertEquals("Sorry, I don't sell glasses of wine.", getReply(npc));

		assertTrue(en.step(player, "buy rose"));
		assertEquals("a rose will cost 50. Do you want to buy it?", getReply(npc));

		assertTrue(en.step(player, "no"));
		assertEquals("Ok, how else may I help you?", getReply(npc));

		assertTrue(en.step(player, "buy rose"));
		assertEquals("a rose will cost 50. Do you want to buy it?", getReply(npc));

		assertTrue(en.step(player, "yes"));
		assertEquals("Sorry, you don't have enough money!", getReply(npc));

		assertTrue(en.step(player, "buy two roses"));
		assertEquals("2 roses will cost 100. Do you want to buy them?", getReply(npc));

		assertTrue(en.step(player, "yes"));
		assertEquals("Sorry, you don't have enough money!", getReply(npc));

		// equip with enough money to buy one rose
		assertTrue(equipWithMoney(player, 50));
		assertTrue(en.step(player, "buy rose"));
		assertEquals("a rose will cost 50. Do you want to buy it?", getReply(npc));

		assertFalse(player.isEquipped("rose"));

		assertTrue(en.step(player, "yes"));
		assertEquals("Congratulations! Here is your rose!", getReply(npc));

		assertTrue(player.isEquipped("rose"));

		// equip with enough money to buy five roses
		assertTrue(equipWithMoney(player, 250));
		assertTrue(en.step(player, "buy 5 roses"));
		assertEquals("5 roses will cost 250. Do you want to buy them?", getReply(npc));

		assertTrue(en.step(player, "yes"));
		assertEquals("Congratulations! Here are your roses!", getReply(npc));

		assertTrue(player.isEquipped("rose", 6));
		assertFalse(player.isEquipped("rose", 7));
	}

}
