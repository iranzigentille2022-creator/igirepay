
package lab1;

public class WalletAccount extends Account {

    public WalletAccount(int id, double balance) {
        super(id, balance);
    }

    @Override
    public void withdraw(double amount) {
        if (Double.isFinite(amount)
                && amount > 0
                && amount <= getBalance()) {

            super.withdraw(amount);

        } else {
            System.out.println(
                    "Withdrawal failed: Invalid amount or insufficient balance.");
        }
    }

    @Override
    public String toString() {
        return "Wallet " + super.toString();
    }
}
