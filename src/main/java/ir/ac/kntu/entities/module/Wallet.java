package ir.ac.kntu.entities.module;

public class Wallet {
    private double balance;

    public Wallet() {
        this.balance = 0.0;
    }

    public void addFunds(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    public boolean deductFunds(double amount) {
        if (this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false; // Insufficient funds
    }

    public double getBalance() {
        return balance;
    }
}
