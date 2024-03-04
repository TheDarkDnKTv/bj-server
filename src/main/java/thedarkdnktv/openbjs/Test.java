package thedarkdnktv.openbjs;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import thedarkdnktv.openbjs.core.*;
import thedarkdnktv.openbjs.enums.Decision;
import thedarkdnktv.openbjs.exception.DecisionNotPossibleException;

import java.util.LinkedList;
import java.util.Scanner;

public class Test {

    private final Logger LOG = LogManager.getLogger("thedarkdnktv.bjclient");

    LinkedList<Runnable> tasks = new LinkedList<>();
    private Scanner scanner;
    private boolean notified;
    private Table table;
    double balance = 100;

    IGameTable.State lastState;
    int lastSlot = -1;

    volatile boolean running = true;

    public static void main(String[] args) {
        new Test().run();
    }

    {
        scanner = new Scanner(System.in);
    }

    void run() {
        var input = new Thread(this::readAndProcessCommand);
        input.setDaemon(true);
        input.start();

        LOG.info("Start");
        table = new Table(1, 3, 5.0D, 500.0D);
        table.setShoe(new Shoe(1, 8, BjUtil.DECK_52_CARDS));
        table.setShuffler(new DigitalRandomShuffler());
        table.init();

        while (this.running) {
            while (!tasks.isEmpty()) {
                tasks.pop().run();
            }

            var state = table.getState();
            if (lastState != state) {
                switch (lastState = state) {
                    case WAITING_FOR_BETS -> LOG.info("Place your bets");
                    case BETTING_TIME -> LOG.info("Betting time is open");
                    case DEALING -> LOG.info("Bets are closed");
                    case IN_GAME -> {
                        LOG.info("Cards dealt");
                        LOG.info("\tDealers open card {}\n", this.table.getDealerHand().getOpenCard());
                    }
                    case GAME_RESOLVED -> {
                        lastSlot = -1;
                        LOG.info("Game finished, result are:");
                        LOG.info("\tDealer hand is {}", table.getDealerHand());
                        for (var i = 0; i < table.getSlotCount(); i++) {
                            var result = table.getResult(i);
                            var hs = result == null ? "NONE" : result.isWin() ? "WIN" : "LOSE";
                            LOG.printf(Level.INFO, "\t%-5s | %-10s | %.1f | %-20s", i, hs,
                                    result == null ? 0.0D : result.getPayout(), table.getSlot(i));
                        }
                    }
                }
            }

            var slot = table.getActiveSlot();
            if (state == IGameTable.State.IN_GAME && lastSlot != slot) {
                lastSlot = slot;
                LOG.info("{} hand turn, hand is {}", slot + 1, table.getCurrentSlot());
            }

            table.update();
        }

        LOG.info("Stopping server");
    }


    void readAndProcessCommand() {
        while (!Thread.interrupted()) {
            var line = scanner.nextLine().toLowerCase();
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
                case "bet" -> {
                    if (balance < table.getMinBet()) {
                        LOG.info("You have insufficient balance");
                        return;
                    }

                    int id;
                    double bet;
                    LOG.info("Enter seat number {}-{}", 1, table.getSlotCount());
                    while (true) {
                        try {
                            id = scanner.nextInt();
                            if (id < 1 || id > table.getSlotCount()) {
                                LOG.info("Please type valid value");
                                continue;
                            }

                            break;
                        } catch (Exception e) {
                            LOG.info("Incorrect input");
                            scanner.nextLine();
                        }
                    }

                    LOG.info("Enter bet amount, your balance is {}", balance);
                    while (true) {
                        try {
                            bet = scanner.nextDouble();
                            if (bet < table.getMinBet() || bet > table.getMaxBet()) {
                                LOG.info("Please ({}) enter bet in range {}-{}", bet, table.getMinBet(), table.getMaxBet());
                                continue;
                            }

                            break;
                        } catch (Exception e) {
                            LOG.info("Incorrect input");
                            scanner.nextLine();
                        }
                    }

                    final var id1 = id - 1;
                    final var bet1 = bet;
                    tasks.push(() -> {
                        table.setBet(id1, bet1);
                        LOG.info("Bet placed");
                    });
                }
                case "ready" -> {
                    LOG.info("Enter seat number");
                    try {
                        var id = scanner.nextInt();
                        if (id >= 1 || id <= table.getSlotCount()) {
                            LOG.info("Please type valid value");
                            var slot = table.getSlot(id - 1);
                            slot.setReady();
                            LOG.info("Seat {} ready: {}", id, slot.isReady());
                        }
                    } catch (Exception e) {
                        LOG.info("Incorrect input");
                        scanner.nextLine();
                    }
                }
                default -> {
                    if (line.startsWith("decision")) {
                        var args = line.split("\s");
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

                            table.setDecision(id, decision);
                        } catch (DecisionNotPossibleException e) {
                            LOG.info("Decision is not possible ({}): {}", e.getMessage(), e.getAllowedDecisions());
                        } catch (Exception e) {
                            LOG.info("Incorrect input");
                        }
                    } else {
                        LOG.info("Unknown command");
                    }
                }
            }
        }
    }
}