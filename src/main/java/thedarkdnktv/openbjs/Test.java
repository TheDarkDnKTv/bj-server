package thedarkdnktv.openbjs;

import com.google.common.eventbus.Subscribe;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import thedarkdnktv.openbjs.core.*;
import thedarkdnktv.openbjs.core.event.TableCardDealtEvent;
import thedarkdnktv.openbjs.core.event.TableDecisionPerformedEvent;
import thedarkdnktv.openbjs.core.event.TableStateEvent;
import thedarkdnktv.openbjs.core.event.TableTurnEvent;
import thedarkdnktv.openbjs.enums.Decision;
import thedarkdnktv.openbjs.exception.DecisionNotPossibleException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

public class Test {

    private final Logger LOG = LogManager.getLogger("thedarkdnktv.bjclient");

    LinkedList<Runnable> tasks = new LinkedList<>();
    Table table;
    volatile double balance = 100;

    IEventBus bus = new GuavaEventBus();

    volatile boolean running = true;

    public static void main(String[] args) {
        new Test().run();
    }

    void run() {
        bus.register(this);

        var input = new Thread(this::readAndProcessCommand);
        input.setDaemon(true);
        input.start();

        LOG.info("Start");
        table = new Table(1, 3, 5.0D, 500.0D, bus);
        table.setShoe(new Shoe(1, 8, BjUtil.DECK_52_CARDS));
        table.setShuffler(new DigitalRandomShuffler());
        table.init();

        while (this.running) {
            while (!tasks.isEmpty()) {
                tasks.pop().run();
            }

            table.update();
        }

        LOG.info("Stopping server");
    }

    void readAndProcessCommand() {
        final var buffer = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (!Thread.interrupted()) {
                var line = buffer.readLine().toLowerCase();
                if (line.isBlank()) {
                    continue;
                }

                switch (line) {
                    case "seats" -> tasks.push(() -> {
                        LOG.info("Seats:");
                        for (var i = 0; i < table.getSlotCount(); i++) {
                            var slot = table.getSlot(i);
                            LOG.printf(Level.INFO, "%-5s | %-20s", i + 1, slot.getState());
                        }
                    });
                    case "balance" -> LOG.info("Balance is: {}", balance);
                    case "stop" -> this.running = false;
                    default -> {
                        this.processCommand(line);
                    }
                }
            }
        } catch (IOException e) {
            LOG.atError()
                .withThrowable(e)
                .log("Command process error");
        }
    }

    void processCommand(String command) {
        var args = command.split("\s");
        if (command.startsWith("decision")) {
            try {
                var id = Integer.parseInt(args[1]) - 1;
                var slot = table.getSlot(id);
                var decision = switch (args[2]) {
                    case "hit" -> Decision.HIT;
                    case "double" -> Decision.DOUBLE_DOWN;
                    case "split" -> Decision.SPLIT;
                    case "stand" -> Decision.STAND;
                    default -> null;
                };

                LOG.info("Decision for {}: {}", id, decision);
                if (decision == Decision.SPLIT || decision == Decision.DOUBLE_DOWN) {
                    if (balance < slot.getBet()) {
                        LOG.info("Not enough money");
                        return;
                    }

                    balance -= slot.getBet();
                }

                tasks.push(() -> {
                    try {
                        table.setDecision(id, decision);
                    } catch (DecisionNotPossibleException e) {
                        LOG.info("Decision is not possible ({}): {}", e.getMessage(), e.getAllowedDecisions());
                    }
                });
            } catch (Exception e) {
                LOG.info("Incorrect input");
            }
        } else if (command.startsWith("bet")) {
            if (balance < table.getMinBet()) {
                LOG.info("You have insufficient balance");
                return;
            }

            try {
                int id = Integer.parseInt(args[1]);
                if (id < 1 || id > table.getSlotCount()) {
                    LOG.info("Please type valid value");
                    return;
                }

                double bet = Double.parseDouble(args[2]);
                if (bet < table.getMinBet() || bet > table.getMaxBet()) {
                    LOG.info("Please ({}) enter bet in range {}-{}", bet, table.getMinBet(), table.getMaxBet());
                    return;
                }

                final var id1 = id - 1;
                final var bet1 = bet;
                tasks.push(() -> {
                    balance -= bet1;
                    table.setBet(id1, bet1);
                    LOG.info("Bet placed");
                });
            } catch (Exception e) {
                LOG.warn("Incorrect input");
            }
        } else if (command.startsWith("ready")) {
            try {
                var id = Integer.parseInt(args[1]);
                if (id < 1 || id > table.getSlotCount()) {
                    LOG.info("Please type valid value");
                    return;
                }

                var slot = table.getSlot(id - 1);
                slot.setReady();
                LOG.info("Seat {} ready: {}", id, slot.isReady());
            } catch (Exception e) {
                LOG.info("Incorrect input");
            }
        } else {
            LOG.info("Unknown command");
        }
    }

    @Subscribe
    public void handleStateChanged(TableStateEvent event) {
        switch (event.state) {
            case WAITING_FOR_BETS -> LOG.info("Place your bets");
            case BETTING_TIME -> LOG.info("Betting time is open");
            case DEALING -> LOG.info("Bets are closed");
            case IN_GAME -> {
                LOG.info("Cards dealt");
                LOG.info("\tDealers open card {}\n", this.table.getDealerHand().getOpenCard());
            }
            case GAME_RESOLVED -> {
                LOG.info("Game finished, result are:");
                LOG.info("\tDealer hand is {}", table.getDealerHand());
                for (var i = 0; i < table.getSlotCount(); i++) {
                    var result = table.getResult(i);
                    var hs = result == null ? "NONE" : result.isWin() ? "WIN" : "LOSE";
                    LOG.printf(Level.INFO, "\t%-5s | %-10s | %.1f | %-20s", i, hs,
                            result == null ? 0.0D : result.getPayout(), table.getSlot(i));

                    if (result != null && result.isWin()) {
                        tasks.push(() -> {
                            balance += (result.getBet() * result.getPayout());
                        });
                    }
                }
            }
        }
    }

    @Subscribe
    public void handleDecisionDone(TableDecisionPerformedEvent event) {
        var slot = table.getSlot(event.handId);
        var score = slot.getScore();
        String scoreStr;
        if (score <= BjUtil.SOFT_SCORE && slot.isSoft()) {
            scoreStr = score + "/" + (score + 10);
        } else {
            scoreStr = Integer.toString(score);
        }

        if (event.state == IHand.HandState.TURN_OVER) {
            if (score > BjUtil.MAX_SCORE) {
                LOG.info("It's too many ({})", score);
            } else {
                LOG.info("Score: {}", scoreStr);
            }
        } else {
            LOG.info("Score: {}, your next turn?", scoreStr);
        }
    }

    @Subscribe
    public void handleDecisionNeeded(TableTurnEvent event) {
        if (event.isDealer()) {
            LOG.info("Dealer turn, hand is {}", table.getDealerHand());
        } else {
            var slot = table.getSlot(event.handId);
            LOG.info("{} hand turn, hand is {}", event.handId + 1, slot);
        }
    }

    @Subscribe
    public void handleCardDealt(TableCardDealtEvent event) {
        if (event.hidden) {
            LOG.info("Card dealt: {}", "<hidden>");
        } else {
            LOG.info("Card dealt: {}", event.card);
        }
    }
}