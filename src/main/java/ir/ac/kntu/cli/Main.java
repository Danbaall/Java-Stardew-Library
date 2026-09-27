package ir.ac.kntu.cli;

import ir.ac.kntu.repo.BorrowHistoryRepo;
import ir.ac.kntu.repo.ItemRepo;
import ir.ac.kntu.repo.PolicyRepo;
import ir.ac.kntu.repo.ReservationRepo;
import ir.ac.kntu.repo.TicketRepo;
import ir.ac.kntu.repo.TransactionRepo;
import ir.ac.kntu.repo.UserRepo;
import ir.ac.kntu.services.AuthService;
import ir.ac.kntu.services.BorrowService;
import ir.ac.kntu.services.DebtService;
import ir.ac.kntu.services.ItemService;
import ir.ac.kntu.services.ManagerService;
import ir.ac.kntu.services.ReservationService;
import ir.ac.kntu.services.TicketService;
import ir.ac.kntu.services.UserService;
import ir.ac.kntu.services.WalletService;
import ir.ac.kntu.util.SeedData;

public class Main {

    public static void main(String[] args) {
        UserRepo userRepo = new UserRepo();
        ItemRepo itemRepo = new ItemRepo();
        BorrowHistoryRepo borrowRepo = new BorrowHistoryRepo();
        TransactionRepo txnRepo = new TransactionRepo();
        TicketRepo ticketRepo = new TicketRepo();
        PolicyRepo policyRepo = new PolicyRepo();

        DebtService debtService = new DebtService(borrowRepo, userRepo);
        AuthService authService = new AuthService(userRepo);
        UserService userService = new UserService(userRepo);
        ItemService itemService = new ItemService(itemRepo);

        ReservationRepo reservationRepo = new ReservationRepo();
        ReservationService reservationService = new ReservationService(reservationRepo, itemRepo);

        BorrowService borrowService = new BorrowService(
                borrowRepo,
                itemRepo,
                debtService,
                reservationService);

        WalletService walletService = new WalletService(
                userRepo,
                txnRepo,
                debtService,
                borrowRepo);

        TicketService ticketService = new TicketService(ticketRepo);
        ManagerService managerService = new ManagerService(
                userRepo,
                policyRepo);

        new SeedData(userRepo, itemRepo).seed();

        new MenuHandler(
                itemRepo,
                borrowRepo,
                txnRepo,
                ticketRepo,
                authService,
                userService,
                itemService,
                borrowService,
                walletService,
                debtService,
                ticketService,
                userRepo,
                managerService, reservationService).run();
    }
}
