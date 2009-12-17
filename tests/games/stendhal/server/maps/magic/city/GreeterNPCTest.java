package games.stendhal.server.maps.magic.city;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import utilities.QuestHelper;
import utilities.ZonePlayerAndNPCTestImpl;

/**
 * Test buying scrolls.
 *
 * @author Martin Fuchs
 */
public class GreeterNPCTest extends ZonePlayerAndNPCTestImpl {

	private static final String ZONE_NAME = "-1_fado_great_cave_e3";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();

		setupZone(ZONE_NAME, new GreeterNPC());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	public GreeterNPCTest() {
		super(ZONE_NAME, "Erodel Bmud");
	}

	@Test
	public void testHiAndBye() {
		final SpeakerNPC npc = getNPC("Erodel Bmud");
		assertNotNull(npc);
		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi Erodel"));
		assertEquals("Salutations, traveller.", getReply(npc));

		assertTrue(en.step(player, "bye"));
		assertEquals("Adieu.", getReply(npc));
	}

	@Test
	public void testBuyScroll() {
		final SpeakerNPC npc = getNPC("Erodel Bmud");
		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi"));
		assertEquals("Salutations, traveller.", getReply(npc));

		assertTrue(en.step(player, "job"));
		assertEquals("I am a wizard, like all who dwell in this magic underground city. We practise #magic here.", getReply(npc));

		assertTrue(en.step(player, "magic"));
		assertEquals("Indeed, enchantments such as our Sunlight Spell to keep the grass and flowers healthy down here. I suppose you are wondering why you have seen traditional enemies such as dark elves and green elves in company together here, let me #explain.", getReply(npc));

		assertTrue(en.step(player, "explain"));
		assertEquals("As a city for wizards only, we have much to learn from one another. Thus, old quarrels are forgotten and we live here in peace.", getReply(npc));

		assertTrue(en.step(player, "quest"));
		assertEquals("Neither can live while the other survives! The Dark Lord must be killed...no ... wait... that was some other time. Forgive me for confusing you, I need nothing.", getReply(npc));

		assertTrue(en.step(player, "buy"));
		assertEquals("Please tell me what you want to buy.", getReply(npc));

		assertTrue(en.step(player, "buy cat"));
		assertEquals("Sorry, I don't sell cats.", getReply(npc));

		assertTrue(en.step(player, "buy someunknownthing"));
		assertEquals("Sorry, I don't sell someunknownthings.", getReply(npc));

		assertTrue(en.step(player, "buy a bottle of wine"));
		assertEquals("Sorry, I don't sell glasses of wine.", getReply(npc));

		assertTrue(en.step(player, "buy scroll"));
		assertEquals("Sorry, I don't sell scrolls.", getReply(npc));

		assertTrue(en.step(player, "buy summon scroll"));
		assertEquals("a summon scroll will cost 300. Do you want to buy it?", getReply(npc));

		assertTrue(en.step(player, "no"));
		assertEquals("Ok, how else may I help you?", getReply(npc));

		assertTrue(en.step(player, "buy summon scroll"));
		assertEquals("a summon scroll will cost 300. Do you want to buy it?", getReply(npc));

		assertTrue(en.step(player, "yes"));
		assertEquals("Sorry, you don't have enough money!", getReply(npc));

		assertTrue(en.step(player, "buy two summon scrolls"));
		assertEquals("2 summon scrolls will cost 600. Do you want to buy them?", getReply(npc));

		// equip with enough money to buy the two scrolls
		assertTrue(equipWithMoney(player, 600));

		assertFalse(player.isEquipped("summon scroll"));
		assertTrue(en.step(player, "yes"));
		assertEquals("Congratulations! Here are your summon scrolls!", getReply(npc));
		assertTrue(player.isEquipped("summon scroll"));

		assertTrue(en.step(player, "buy home scroll"));
		assertEquals("a home scroll will cost 375. Do you want to buy it?", getReply(npc));

		assertTrue(equipWithMoney(player, 300));
		assertTrue(en.step(player, "yes"));
		assertEquals("Sorry, you don't have enough money!", getReply(npc));

		assertTrue(en.step(player, "buy home scroll"));
		assertEquals("a home scroll will cost 375. Do you want to buy it?", getReply(npc));

		// add another 75 coins to be able to buy the scroll
		assertTrue(equipWithMoney(player, 75));

		assertFalse(player.isEquipped("home scroll"));
		assertTrue(en.step(player, "yes"));
		assertEquals("Congratulations! Here is your home scroll!", getReply(npc));
		assertTrue(player.isEquipped("home scroll"));
	}

	@Test
	public void testSellScroll() {
		final SpeakerNPC npc = getNPC("Erodel Bmud");
		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi"));
		assertEquals("Salutations, traveller.", getReply(npc));

		// There is not yet a trigger for selling things to Erodel
		assertFalse(en.step(player, "sell summon scroll"));
	}

}
