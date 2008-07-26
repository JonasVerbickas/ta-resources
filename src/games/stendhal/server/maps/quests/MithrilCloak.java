package games.stendhal.server.maps.quests;

import games.stendhal.common.Grammar;
import games.stendhal.common.MathHelper;
import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPC.ChatAction;
import games.stendhal.server.entity.npc.SpeakerNPC.ChatCondition;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.parser.Expression;
import games.stendhal.server.entity.npc.parser.JokerExprMatcher;
import games.stendhal.server.entity.npc.parser.Sentence;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * QUEST: Mithril Cloak
 * <p>
 * PARTICIPANTS:
 * <ul>
 * <li>Ida, a seamstress in Ados.</li>
 * <li>Hogart, a retired master dwarf smith, forgotten below the dwarf mines in
 * Orril.</li>
 * <li>Terry, the dragon hatcher in semos caves.</li>
 * </ul>
 * <p>
 * STEPS:
 * <ul>
 * <li>Ida needs machine fixed</li>
 * <li>Once machine fixed and if you have done mithril shield quest, Ida offers you cloak</li>
 * <li>Cloak needs fabric first then scissors then needles<li>
 * <li>Hogart makes the scissors which need eggshells</li>
 * <li>Terry swaps the eggshells for poisons</li>
 * <li>Needles come from Ritati Dragontracker</li>
 * </ul>
 * <p>
 * REWARD:
 * <ul>
 * <li>Mithril Cloak</li>
 * <li> XP</li>
 * </ul>
 * <p>
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 */
public class MithrilCloak extends AbstractQuest {

	private static final int REQUIRED_MINUTES_SCISSORS = 10;
	private static final int REQUIRED_MINUTES_CLASP = 60;

	private static final int REQUIRED_HOURS_SEWING = 24;

	private static final String QUEST_SLOT = "mithril_cloak";
	private static final String MITHRIL_SHIELD_QUEST = "mithrilshield_quest";

	private static final String FABRIC = "mithril fabric";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void offerQuestStep() {
		final SpeakerNPC npc = npcs.get("Ida");
		

		// player asks about quest, they haven't started it yet
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new OrCondition(new QuestNotStartedCondition(QUEST_SLOT), new QuestInStateCondition(QUEST_SLOT, "rejected")),				
				ConversationStates.QUEST_OFFERED, 
				"My sewing machine is broken, will you help me fix it?",
				null);

		// Player says yes they want to help 
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING,
			null,			
				new ChatAction() {
							@Override
							public void fire(final Player player,
									final Sentence sentence,
									final SpeakerNPC npc) {
								final List<String> PARTS_LIST = Arrays.asList("leather armor", "oil", "bobbin");
								final String parts = Rand.rand(PARTS_LIST);
								if("leather armor".equals(parts)){
									npc.say("Thank you! It needs a piece of leather to fix it. Please fetch me " 
											+ Grammar.a_noun(parts) + " and come back as soon as you can.");
								} else {
									npc.say("Thank you! It needs " 
											+ Grammar.a_noun(parts)
												+ ", I'm ever so grateful for your help.");
								}
								new SetQuestAndModifyKarmaAction(QUEST_SLOT, "machine;" 
																 + parts, 15.0).fire(player, sentence, npc);
							}
				}
				);
		
		// player said no they didn't want to help
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Oh dear, I don't know what I can do without a decent sewing machine. But don't worry I won't bother you any longer!",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));


		// player asks for quest but they already did it	
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"You've already completed the only quest that I have for you.",
				null);
		
		//player fixed the machine but hadn't got mithril shield. 
		// they return and ask for quest but they still haven't got mithril shield
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new NotCondition(new QuestCompletedCondition(MITHRIL_SHIELD_QUEST)),
								 new OrCondition(new QuestInStateCondition(QUEST_SLOT, "need_mithril_shield"),
												 new QuestInStateCondition(QUEST_SLOT, "fixed_machine"))
								 ),
				ConversationStates.ATTENDING, 
								 "I don't have anything for you until you have proved yourself worthy of carrying mithril items, by getting the mithril shield.",
				null);


		// player fixed the machine but hadn't got mithril shield at time or didn't ask to hear more about the cloak. 
		// when they have got it and return to ask for quest she offers the cloak
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(
								 new QuestCompletedCondition(MITHRIL_SHIELD_QUEST),
								 new OrCondition(new QuestInStateCondition(QUEST_SLOT, "need_mithril_shield"),
												 new QuestInStateCondition(QUEST_SLOT, "fixed_machine"))
								 ),
				ConversationStates.QUEST_2_OFFERED, 
				"Congratulations, you completed the quest for the mithril shield! Now, I have another quest for you, do you want to hear it?",
				null);




	}

	
	private void fixMachineStep() {

		final SpeakerNPC npc = npcs.get("Ida");

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("sewing", "machine", "sewing machine", "leather armor", "oil", "bobbin", "task", "quest"),
				new QuestStateStartsWithCondition(QUEST_SLOT, "machine"),
				ConversationStates.QUEST_ITEM_QUESTION,
				"My sewing machine is still broken, did you bring anything to fix it?",
				null);

			// we stored the needed part name as part of the quest slot
			npc.add(ConversationStates.QUEST_ITEM_QUESTION,
					ConversationPhrases.YES_MESSAGES,
					null,
					ConversationStates.ATTENDING,
					null,
					new SpeakerNPC.ChatAction() {
						@Override
						public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
							final String[] questslot = player.getQuest(QUEST_SLOT).split(";");
							if (player.isEquipped(questslot[1])){
								player.drop(questslot[1]);
								npc.say("Thank you so much! Listen, I must repay the favour, and I have a wonderful idea. Do you want to hear more?");
								player.addXP(100);
								player.setQuest(QUEST_SLOT,"fixed_machine");
								player.notifyWorldAboutChanges();
								npc.setCurrentState(ConversationStates.QUEST_2_OFFERED);
							} else {
								npc.say("No, you don't have the " + Grammar.fullForm(questslot[1]) + " I need. What a shame.");
							}
						}
					});
				
		   npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				   ConversationPhrases.NO_MESSAGES,
				   null,
				   ConversationStates.ATTENDING,
				   "Ok, well if there's anything else I can help you with just say. Don't forget about me though!",
				   null);

		   //TODO: Say how to get fabric here
		   npc.add(ConversationStates.QUEST_2_OFFERED,
				   ConversationPhrases.YES_MESSAGES,
				   new QuestCompletedCondition(MITHRIL_SHIELD_QUEST),
				   ConversationStates.ATTENDING,		   
				   "I will make you the most amazing cloak of mithril. You just need to get me the fabric and any tools I need! First please bring me a yard of " + FABRIC + ".",
				   new SetQuestAndModifyKarmaAction(QUEST_SLOT, "need_fabric", 10.0));
					

			// player asks for quest but they haven't completed mithril shield quest
			npc.add(ConversationStates.QUEST_2_OFFERED,
				ConversationPhrases.YES_MESSAGES, 
				new AndCondition(
								 new NotCondition(new QuestCompletedCondition(MITHRIL_SHIELD_QUEST)),
								 new QuestStartedCondition(MITHRIL_SHIELD_QUEST)
								 ),
				ConversationStates.ATTENDING, 
				"Oh, I see you are already on a quest to obtain a mithril shield. You see, I was going to offer you a mithril cloak. But you should finish that first. Come back when you've finished the mithril shield quest and we will speak again.",
				new SetQuestAction(QUEST_SLOT,"need_mithril_shield"));
			
			// player asks for quest but they haven't completed mithril shield quest
			npc.add(ConversationStates.QUEST_2_OFFERED,
					ConversationPhrases.YES_MESSAGES,
					new QuestNotStartedCondition(MITHRIL_SHIELD_QUEST),
					ConversationStates.ATTENDING, 
					"There are legends of a wizard called Baldemar, in the famous underground magic city, who will forge a mithril shield for those who bring him what it needs. You should meet him and do what he asks. Once you have completed that quest, come back here and speak with me again. I will have another quest for you.",
					new SetQuestAction(QUEST_SLOT,"need_mithril_shield"));

			npc.add(ConversationStates.QUEST_2_OFFERED,
					ConversationPhrases.NO_MESSAGES,
					null,
					ConversationStates.ATTENDING,
					"Ok then obviously you don't need any mithril items! Forgive me for offering to help...!",
					null);

	
	}

	private void getFabricStep() {
		//I (kymara) don't know what is meant to go here.
	}

	private void giveFabricStep() {	

		final SpeakerNPC npc = npcs.get("Ida");

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("fabric", "mithril", "cloak", "mithril cloak", "task", "quest"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "need_fabric"),new PlayerHasItemWithHimCondition(FABRIC)),
				ConversationStates.ATTENDING,
				"Wow you got the " + FABRIC + " , that didn't take as long as I expected! Now, to cut it I need magical scissors, if you would go get them from #Hogart. I will be waiting for you to return.",
				new MultipleActions(
									 new DropItemAction("fabric"), 
									 new SetQuestAndModifyKarmaAction(QUEST_SLOT, "need_scissors", 10.0)
				));

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("fabric", "mithril", "cloak", "mithril cloak", "task", "quest"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "need_fabric"),
								 new NotCondition(new PlayerHasItemWithHimCondition(FABRIC))
								 ),
				ConversationStates.ATTENDING,
				"I'm still waiting for the " + FABRIC + " so I can start work on your mithril cloak.",				
				null);

		npc.addReply("Hogart","He's that grumpy old dwarf in the Or'ril mines. I already sent him a message saying I wanted some new scissors but he didn't respond. Well, what he lacks in people skills he makes up for in his metal work.");
	}

	private void getScissorsStep() {

		// Careful not to overlap with any states from VampireSword quest

		final SpeakerNPC npc = npcs.get("Hogart");

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("scissors", "magical", "magical scissors", "ida", "mithril", "cloak", "mithril cloak"),
			new QuestInStateCondition(QUEST_SLOT, "need_scissors"),
			ConversationStates.SERVICE_OFFERED,
			null,
			new SpeakerNPC.ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final SpeakerNPC engine) {
					final int neededEggshells = Rand.randUniform(2,4);
					engine.say("Ah yes, Ida sent me a message about some magical scissors. I need one each of an iron bar and a mithril bar, and also " + Integer.toString(neededEggshells) + " magical #eggshells. Ask me about #scissors again when you return with those items.");
					// store the number of needed eggshells in the quest slot so he remembers how many he asked for
					player.setQuest(QUEST_SLOT, "need_eggshells;"+Integer.toString(neededEggshells));
				}
			});

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("scissors", "magical", "magical scissors", "ida", "mithril", "cloak", "mithril cloak"),
			new QuestStateStartsWithCondition(QUEST_SLOT, "need_eggshells"),
			ConversationStates.SERVICE_OFFERED,
			"So, did you bring the items I need for the magical scissors?", null);
									

		npc.add(
			ConversationStates.SERVICE_OFFERED,
			"eggshells", 
			null,
			ConversationStates.ATTENDING,
			"They must be from dragon eggs. I guess you better find someone who dares to hatch dragons!",
			null);

		// we can't use the nice ChatActions here because the needed number is stored in the quest slot i.e. we need a fire
		npc.add(
			ConversationStates.SERVICE_OFFERED,
			ConversationPhrases.YES_MESSAGES, 
			new QuestStateStartsWithCondition(QUEST_SLOT, "need_eggshells"),
			ConversationStates.ATTENDING,
			null,
			new SpeakerNPC.ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
					final String[] questslot = player.getQuest(QUEST_SLOT).split(";");
					final int neededEggshells = Integer.valueOf(questslot[1]);
					if (player.isEquipped("iron")
						&& player.isEquipped("mithril bar")
						&& player.isEquipped("magical eggshells", neededEggshells)) 
						{
							player.drop("iron");
							player.drop("mithril bar");
							player.drop("magical eggshells", neededEggshells);
							npc.say("Good. It will take me some time to make these, come back in " 
									   + REQUIRED_MINUTES_SCISSORS + " minutes to get your scissors.");
							player.addXP(100);
							player.setQuest(QUEST_SLOT, "makingscissors;" + System.currentTimeMillis());
							player.notifyWorldAboutChanges();
						} else {
							npc.say("Liar, you don't have everything I need. Ask me about #scissors again when you have an iron bar, a mithril bar, and " 
									+ questslot[1] + " magical eggshells. And don't be wasting my time!");
						}
				}
			});

		// player says they didn't bring the stuff yet
		npc.add(
			ConversationStates.SERVICE_OFFERED,
			ConversationPhrases.NO_MESSAGES, 
			null,
			ConversationStates.ATTENDING,
			"What are you still here for then? Go get them!",
			null);

		npc.add(ConversationStates.ATTENDING, 
			Arrays.asList("scissors", "magical", "magical scissors", "ida", "mithril", "cloak", "mithril cloak"),
			new QuestStateStartsWithCondition(QUEST_SLOT, "makingscissors;"),
			ConversationStates.ATTENDING, null, new SpeakerNPC.ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
					final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
					// minutes -> milliseconds
					final long delay = REQUIRED_MINUTES_SCISSORS * MathHelper.MILLISECONDS_IN_ONE_MINUTE;
					final long timeRemaining = (Long.parseLong(tokens[1]) + delay)
							- System.currentTimeMillis();
					if (timeRemaining > 0L) {
						npc.say("Pff you're impatient aren't you? I haven't finished making the scissors yet, come back in "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L)) + ".");
						return;
					}
					npc.say("Ah, thanks for reminding me. Here, Ida's scissors are ready. You better take them to her next as I don't know what she wanted them for.");
					player.addXP(100);
					player.addKarma(15);
					final Item scissors = SingletonRepository.getEntityManager().getItem(
									"magical scissors");
					scissors.setBoundTo(player.getName());
					player.equip(scissors, true);
					player.setQuest(QUEST_SLOT, "got_scissors");
					player.notifyWorldAboutChanges();
				}
			});

	}

	private void getEggshellsStep() {

		final int REQUIRED_POISONS = 6;

		final SpeakerNPC npc = npcs.get("Terry");

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("eggshells", "magical", "magical eggshells", "scissors", "hogart", "ida", "cloak", "mithril cloak", "specials"),
				new QuestStateStartsWithCondition(QUEST_SLOT, "need_eggshells"),
				ConversationStates.QUEST_ITEM_QUESTION,
				"Sure, I sell eggshells. They're not worth much to me. I'll swap you one eggshell for every " + Integer.toString(REQUIRED_POISONS) + " disease poisons you bring me. I need it to kill the rats you see. Anyway, how many eggshells was you wanting?",
				null);

		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				// match for all numbers as trigger expression
				"NUM", new JokerExprMatcher(),
				new ChatCondition() {
					@Override
                    public boolean fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
						final Expression number = sentence.getNumeral();

						if (number != null) {
    						final int required = number.getAmount();

    						// don't let them buy less than 1 or more than, say, 5000
    						if ((required >= 1) && (required <= 5000)) {
    							return true;
    						}
						}

    					return false;
                    }
				}, ConversationStates.ATTENDING, null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {

                        final int required = (sentence.getNumeral().getAmount());
						if( player.drop("disease poison",required*REQUIRED_POISONS) ){
							npc.say("Ok, here's your " + Integer.toString(required) + " eggshells. Enjoy!");
							new EquipItemAction("magical eggshells", required, true).fire(player, sentence, npc);
						} else {
							npc.say("Ok, ask me again when you have " + Integer.toString(required*REQUIRED_POISONS) + " disease poisons with you.");
						}						
					}
				});

		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				Arrays.asList("no", "none", "nothing"),
				null,
				ConversationStates.ATTENDING,
				"No problem. Anything else I can help with, just say.",
				null);

 	}

	private void giveScissorsStep() {

		final SpeakerNPC npc = npcs.get("Ida");

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("scissors", "magical", "magical scissors", "mithril", "cloak", "mithril cloak", "task", "quest"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "got_scissors"),new PlayerHasItemWithHimCondition("magical scissors")),
				ConversationStates.ATTENDING,
				"You brought those magical scissors! Excellent! Now that I can cut the fabric I need a magical needle. You can buy one from a trader in the abandoned keep of Ados mountains, #Ritati Dragon something or other. Just go to him and ask for his 'specials'.",
				new MultipleActions(
									 new DropItemAction("magical scissors"), 
									 new SetQuestAndModifyKarmaAction(QUEST_SLOT, "need_needle;", 10.0), 
									 new IncreaseXPAction(100)
									 )
				);

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("scissors", "magical", "magical scissors", "mithril", "cloak", "mithril cloak", "task", "quest"),
				new OrCondition(
								new QuestInStateCondition(QUEST_SLOT, "need_scissors"),
								new QuestStateStartsWithCondition(QUEST_SLOT, "makingscissors;"),
								new AndCondition(new QuestInStateCondition(QUEST_SLOT, "got_scissors"),
												 new NotCondition(new PlayerHasItemWithHimCondition("magical scissors")))
								),
				ConversationStates.ATTENDING,
				"Where are the magical scissors you are supposed to be getting from #Hogart?",				
				null);

		npc.addReply("Ritati","He's somewhere in the abandoned keep in the mountains north east from here.");
	}



	private void getNeedleStep() {

		// There is meant to be something about telling him a joke from a book in here but for now we KISS and just try the selling thing
		final int NEEDLE_COST = 1500;

		final SpeakerNPC npc = npcs.get("Ritati Dragontracker");

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("needle", "magical", "magical needle", "ida", "cloak", "mithril cloak", "specials"),
				new QuestStateStartsWithCondition(QUEST_SLOT, "need_needle"),
				ConversationStates.QUEST_ITEM_QUESTION,
				"I have some magical needles but they cost a pretty penny, "
				+ Integer.toString(NEEDLE_COST) + " pieces of money to be precise. Do you want to buy one?",
				null);

		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.YES_MESSAGES, 
				new PlayerHasItemWithHimCondition("money",NEEDLE_COST),
				ConversationStates.ATTENDING,
				"Ok, here you are. Be careful with them, they break easy.",				
				new MultipleActions(
									 new DropItemAction("money",NEEDLE_COST), 
									 new EquipItemAction("magical needle")
									 ));

		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.YES_MESSAGES, 
				new NotCondition(new PlayerHasItemWithHimCondition("money",NEEDLE_COST)),
				ConversationStates.ATTENDING,
				"What the ... you don't have enough money! Get outta here!",				
				null);

		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.NO_MESSAGES, 
				null,
				ConversationStates.ATTENDING,
				"Ok, no pressure, no pressure. Maybe you'll like some of my other #offers.",
				null);

	}

	private void giveNeedleStep() {

		final SpeakerNPC npc = npcs.get("Ida");

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("needle", "magical needle", "magical", "mithril", "cloak", "mithril cloak", "task", "quest"),
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "need_needle"), new PlayerHasItemWithHimCondition("magical needle")),
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(
									 new SpeakerNPC.ChatAction() {
										 @Override
										 public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
											 final String[] questslot = player.getQuest(QUEST_SLOT).split(";");
											 int needles;
											 if (questslot.length >1) {
												 // if the split works, we had stored a needle number before
												 needles = Integer.parseInt(questslot[1]);
												 npc.say("I'm really sorry about the previous needle breaking. I'll start work again on your cloak," +
														 " please return in another " + REQUIRED_HOURS_SEWING + " hours.");
											 } else {
												 // it wasn't split with a number.
												 // so this is the first time we brought a needle
												 npc.say("Looks like you found Ritatty then, good. I'll start on the cloak now!" +
														 " A seamstress needs to take her time, so return in " + REQUIRED_HOURS_SEWING + " hours.");
												 // ida breaks needles - she will need 1 - 3
												 needles = Rand.randUniform(1,3);
											 }											
											 player.setQuest(QUEST_SLOT, "sewing;" + System.currentTimeMillis() + ";" + needles);
										 }
									 },
									 new DropItemAction("magical needle")
									 )
				);

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("needle", "magical needle", "magical", "mithril", "cloak", "mithril cloak", "task", "quest"),
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "need_needle"),
								 new NotCondition(new PlayerHasItemWithHimCondition("magical needle"))
								 ),
				ConversationStates.ATTENDING,
				"Where is the magical needle you are supposed to be getting from Ritati thingummy bloke?",				
				null);
	}

	private void sewingStep() {

		final SpeakerNPC npc = npcs.get("Ida");

		// the quest slot that starts with sewing is the form "sewing;number;number" where the first number is the time she started sewing
		// the second number is the number of needles that she's still going to use - player doesn't know number

		npc.add(ConversationStates.ATTENDING, 
				Arrays.asList("magical", "mithril", "cloak", "mithril cloak", "task", "quest"),
				new QuestStateStartsWithCondition(QUEST_SLOT, "sewing;"),
				ConversationStates.ATTENDING, null, new SpeakerNPC.ChatAction() {
						@Override
						public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
							final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
							// hours -> milliseconds
							final long delay = REQUIRED_HOURS_SEWING * MathHelper.MILLISECONDS_IN_ONE_HOUR;
							final long timeRemaining = (Long.parseLong(tokens[1]) + delay)
								- System.currentTimeMillis();
							if (timeRemaining > 0L) {
								npc.say("I'm still sewing your cloak, come back in "
										+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L)) + " - and don't rush me, or I'm more likely to break the needle.");
								return;
							}
							// ida breaks needles, but if it is the last one,
							// she pricks her finger on that needle
							if (Integer.valueOf(tokens[2])==1 ){
								npc.say("Ouch! I pricked my finger on that needle! I feel woozy ...");
								player.setQuest(QUEST_SLOT, "twilight_zone");
							} else {
								npc.say("These magical needles are so fragile, I'm sorry but you're going to have to get me another, the last one broke. Hopefully Ritati still has plenty.");
								final int needles = Integer.parseInt(tokens[2]) - 1;
								player.setQuest(QUEST_SLOT, "need_needle;" + needles );
							}							
				}
			});

	}

	private void getMossStep() {

		// Careful not to overlap with quest states in RainbowBeans quest

		final int MOSS_COST = 3000;

		final SpeakerNPC npc = npcs.get("Pdiddi");

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("moss", "magical", "twilight", "ida", "cloak", "mithril cloak", "specials"),
				new QuestStateStartsWithCondition(QUEST_SLOT, "twilight_zone"),
				ConversationStates.QUEST_ITEM_QUESTION,
				"Keep it quiet will you! Yeah, I got moss, it's "
				+ Integer.toString(MOSS_COST) + " money each. How many do you want?",
				null);

		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				// match for all numbers as trigger expression
				"NUM", new JokerExprMatcher(),
				new ChatCondition() {
					@Override
                    public boolean fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
						final Expression number = sentence.getNumeral();

						if (number != null) {
    						final int required = number.getAmount();

    						// don't let them buy less than 1 or more than, say, 5000
    						if ((required >= 1) && (required <= 5000)) {
    							return true;
    						}
						}

    					return false;
                    }
				}, ConversationStates.ATTENDING, null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {

                        final int required = (sentence.getNumeral().getAmount());
						if( player.drop("money" , required*MOSS_COST) ){
							npc.say("Ok, here's your " + Integer.toString(required) + " pieces of twilight moss. Don't take too much at once.");
							new EquipItemAction("twilight moss", required, true).fire(player, sentence, npc);
						} else {
							npc.say("Ok, ask me again when you have enough money.");
						}						
					}
				});

		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				Arrays.asList("no", "none", "nothing"),
				null,
				ConversationStates.ATTENDING,
				"Ok, whatever you like.",
				null);
	}

	private void twilightZoneStep() {

		// i don't know if player is meant to visit ida in the twilight zone or not. if yes it has to be a different name i.e. lda
		// also don't know when she asks you to take the striped cloak to josephine

	}

	private void takeStripedCloakStep() {

		// Deliberately overlap with conversation states from the cloak collector quests.
		// Since if you're on one of these quests she will always ask 'did you bring any cloaks?' 
		// and waits for you to say yes or the name of the cloak you brought
		// if she just said about 'blue striped cloak' "well i don't want that" then that's confusing for player
		// so we let player give her that cloak even if she was asking about the other quests
		// of course, she will only take it from ida if player was in quest state for teh mithril cloak quest of "taking_striped_cloak"

		final SpeakerNPC npc = npcs.get("Josephine");

		// overlapping with CloaksCollector quest deliberately
		npc.add(ConversationStates.QUESTION_1, "blue striped cloak", new QuestInStateCondition(QUEST_SLOT, "taking_striped_cloak"),
			ConversationStates.QUESTION_1, null,
			new SpeakerNPC.ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
						if( player.drop("blue striped cloak") ){
							npc.say("Oh, wait, that's from Ida isn't it?! Oh yay! Thank you! Please tell her thanks from me!!");
							player.setQuest(QUEST_SLOT, "gave_striped_cloak");
							npc.setCurrentState(ConversationStates.ATTENDING);
						} else {
							npc.say("You don't have a blue striped cloak with you.");
						}						
					}
			});

		// overlapping with CloaksCollector2 quest deliberately
		npc.add(ConversationStates.QUESTION_2, "blue striped cloak", new QuestInStateCondition(QUEST_SLOT, "taking_striped_cloak"),
				ConversationStates.QUESTION_2, null,
				new SpeakerNPC.ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
						if( player.drop("blue striped cloak") ){
							npc.say("Oh, wait, that's from Ida isn't it?! Oh yay! Thank you! Please tell her thanks from me!!");
							npc.setCurrentState(ConversationStates.ATTENDING);
							player.setQuest(QUEST_SLOT, "gave_striped_cloak");
						} else {
							npc.say("You don't have a blue striped cloak with you.");
						}						
					}
			});
				

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("blue striped cloak", "mithril", "mithril cloak", "ida"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "taking_striped_cloak"),new PlayerHasItemWithHimCondition("blue striped cloak")),
				ConversationStates.ATTENDING,
				"Oh that's from Ida isn't it?! Oh yay! Thank you! Please tell her thanks from me!!",
				new MultipleActions(
									 new DropItemAction("blue striped cloak"), 
									 new SetQuestAction(QUEST_SLOT, "gave_striped_cloak") 
									 
									 )
				);


	}
	private void getClaspStep() {

		// don't overlap with any states from producer adder since he is a mithril bar producer
		
		final SpeakerNPC npc = npcs.get("Pedinghaus");

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("clasp", "mithril clasp", "ida", "cloak", "mithril cloak"),
			new QuestInStateCondition(QUEST_SLOT, "need_clasp"),
			ConversationStates.SERVICE_OFFERED,
			"A clasp? Whatever you say! I am still so happy from that letter you brought me, it would be my pleasure to make something for you. I only need one mithril bar. Do you have it?",
			null);


		npc.add(
			ConversationStates.SERVICE_OFFERED,
			ConversationPhrases.YES_MESSAGES, 
			new QuestInStateCondition(QUEST_SLOT, "need_clasp"),
			ConversationStates.ATTENDING,
			null,
			new SpeakerNPC.ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
					if (player.isEquipped("mithril bar")) 
						{	player.drop("mithril bar");
							npc.say("What a lovely piece of mithril that is, even if I do say so myself ... Good, please come back in " 
									   + REQUIRED_MINUTES_CLASP + " minutes and hopefully your clasp will be ready!");
							player.setQuest(QUEST_SLOT, "forgingclasp;" + System.currentTimeMillis());
							player.notifyWorldAboutChanges();
						} else {
							npc.say("You can't fool an old wizard, and I'd know mithril when I see it. Come back when you have at least one bar.");
						}
				}
			});

		// player says they didn't bring the stuff yet
		npc.add(
			ConversationStates.SERVICE_OFFERED,
			ConversationPhrases.NO_MESSAGES, 
			null,
			ConversationStates.ATTENDING,
			"Well, if you should like me to cast any mithril bars just say.",
			null);

		npc.add(ConversationStates.ATTENDING, 
			Arrays.asList("clasp", "mithril clasp", "ida", "cloak", "mithril cloak"),
			new QuestStateStartsWithCondition(QUEST_SLOT, "forgingclasp;"),
			ConversationStates.ATTENDING, null, new SpeakerNPC.ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final SpeakerNPC npc) {
					final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
					// minutes -> milliseconds
					final long delay = REQUIRED_MINUTES_CLASP * MathHelper.MILLISECONDS_IN_ONE_MINUTE;
					final long timeRemaining = (Long.parseLong(tokens[1]) + delay)
							- System.currentTimeMillis();
					if (timeRemaining > 0L) {
						npc.say("I haven't finished yet, please return in "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L)) + ".");
						return;
					}
					npc.say("Here, your clasp is ready!");
					player.addXP(100);
					player.addKarma(15);
					final Item clasp = SingletonRepository.getEntityManager().getItem(
									"mithril clasp");
					clasp.setBoundTo(player.getName());
					player.equip(clasp, true);
					player.setQuest(QUEST_SLOT, "got_clasp");
					player.notifyWorldAboutChanges();
				}
			});

	}

	@Override
	public void addToWorld() {
		super.addToWorld();

		offerQuestStep();
		fixMachineStep();
		getFabricStep();
		giveFabricStep();	
		getScissorsStep();
		getEggshellsStep();
		giveScissorsStep();
		getNeedleStep();
		giveNeedleStep();
		sewingStep();
		getMossStep();
		twilightZoneStep();
		takeStripedCloakStep();
		getClaspStep();
		//giveClaspStep();
		

	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("FIRST_CHAT");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("QUEST_REJECTED");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add("QUEST_ACCEPTED");
		}
		if (player.getQuest(QUEST_SLOT).startsWith("making;")) {
			res.add("MAKING SCISSORS");
		}
		if (questState.equals("done")) {
			res.add("DONE");
		}
		return res;
	}
}
