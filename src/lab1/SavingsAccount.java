
package lab1;

public class SavingsAccount extends Account {

    public SavingsAccount(int id, double balance) {
        super(id, balance);
    }

    @Override
    public void withdraw(double amount) {
        double fee = 100;

        if (Double.isFinite(amount)
                && amount > 0
                && amount + fee <= getBalance()) {

            super.withdraw(amount + fee);

            System.out.println("Withdrawal fee: " + fee + " RWF");

        } else {
            System.out.println(
                    "Withdrawal failed: Invalid amount or insufficient balance.");
        }
    }

    @Override
    public String toString() {
        return "Savings " + super.toString();
    }
}
