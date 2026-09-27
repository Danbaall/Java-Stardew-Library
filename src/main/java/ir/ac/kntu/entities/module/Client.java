package ir.ac.kntu.entities.module;

import java.time.LocalDate;

import ir.ac.kntu.entities.enums.Role;

public abstract class Client extends User implements Manageable {

    private static final String SEP = " | ";

    private Wallet wallet;

    public Client() {
        //this is for jackson
    }

    public Client(String name, String email, String password, Role role, String phoneNumber) {
        super(name, password, email, role, phoneNumber);
        this.wallet = new Wallet();
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public abstract int getMaxBorrowLimit();

    public abstract int getMaxBorrowDays();

    public boolean canBorrow(int activeBorrowCount) {
        return activeBorrowCount < getMaxBorrowLimit();
    }

    public LocalDate calculateDueDate() {
        return LocalDate.now().plusDays(getMaxBorrowDays());
    }

    @Override
    public String getSummary() {
        double balance = (wallet != null) ? wallet.getBalance() : 0.0;
        return getRole() + SEP + getUserName() + SEP + getId()
                + SEP + getEmail() + " | active=" + isActive() + " | wallet=$" + balance;
    }

    @Override
    public String toString() {
        double balance = (wallet != null) ? wallet.getBalance() : 0.0;
        return super.toString() + " [wallet=" + balance + "]";
    }
}
