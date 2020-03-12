import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class KredxSlices {

	public static void main(String[] args) {
		KredxSlices kredxSlices = new KredxSlices();
		kredxSlices.createBatch();
	}
	public static void printBatches(List<KredxBatch> finalBatches) {
		System.out.println("Batch Size\tBatch Total\tBatch Contents");

		for (KredxBatch batch : finalBatches) {
			StringBuilder x = new StringBuilder();
			for (Double invoiceAmount : batch.batch)
				x.append(String.format("%.2f", invoiceAmount)).append(", ");
			System.out.println(batch.batchSize + "\t\t" + String.format("%.2f", batch.batchTotal) + "\t\t" + x);
		}
	}

	public void createBatch() {
		int invoice_count = ThreadLocalRandom.current().nextInt(1, 100000);
		System.out.println("Invoice count: " + invoice_count);
		double min_invoice_value = 100.00;
		double max_invoice_value = 10000000.00;
		Double min_loan_value = 300000.00;
		Double max_loan_value = 50000000.00;
		int optimum_batch_size = 20;
		int max_batch_size = 59;
		List<Double> invoices = new ArrayList<>();
		for (int i = 0; i < invoice_count; i++) {
			invoices.add(ThreadLocalRandom.current().nextDouble(min_invoice_value, max_invoice_value));
		}
		System.out.println("Unsorted array of invoices: " + invoices.toString());
		Collections.sort(invoices);
		System.out.println("Sorted array of invoices: " + invoices.toString());
		int count = 0;
		List<KredxBatch> batches = new ArrayList<>();
		int right = invoices.size() - 1;
		while (count < invoices.size()) {
			KredxBatch batch = new KredxBatch();
			while (batch.batchTotal <= max_loan_value && batch.batchSize <= optimum_batch_size && right >= 0) {
				if (batch.batchTotal + invoices.get(right) > max_loan_value)
					break;
				batch.batch.add(invoices.get(right));
				batch.batchTotal += invoices.get(right--);
				batch.batchSize++;
			}
			if (batch.batchSize > 0)
				batches.add(batch);
			count++;
		}
		boolean retryRequired = true;
		int retryCount = 0;
		while (retryRequired && retryCount < 10) {
			retryRequired = false;
			for (int i = batches.size() - 1; i >= 0; i--) {
				KredxBatch lastBatch = batches.get(i);
				if (lastBatch.batchTotal <= min_loan_value || lastBatch.batchSize < max_batch_size / 2) {
					int invoiceIdToShift = lastBatch.batchSize - 1;
					if (invoiceIdToShift >= 0) {
						for (int j = 0; j < i; j++) {
							KredxBatch firstBatch = batches.get(j);
							while (firstBatch.batchSize <= max_batch_size && invoiceIdToShift >= 0 && (firstBatch.batchTotal + lastBatch.batch.get(invoiceIdToShift) <= max_loan_value)) {
								firstBatch.batch.add(lastBatch.batch.get(invoiceIdToShift));
								firstBatch.batchTotal += lastBatch.batch.get(invoiceIdToShift--);
								firstBatch.batchSize++;
							}
							batches.set(j, firstBatch);
							if (invoiceIdToShift < 0) {
								batches.get(i).batchSize = 0;
								break;
							}
						}
					}
					if (invoiceIdToShift >= 0) {
						retryRequired = true;
						break;
					}
				}
			}
			retryCount++;
		}
		List<KredxBatch> finalBatches = new ArrayList<>();
		List<KredxBatch> inCompleteBatches = new ArrayList<>();
		for (KredxBatch batch : batches) {
			if (batch.batchSize > 0)
				if (batch.batchTotal < min_loan_value)
					inCompleteBatches.add(batch);
				else
					finalBatches.add(batch);
		}
		System.out.println("Batches to be sent : ");
		printBatches(finalBatches);
		System.out.println("\n\n==================================================\nIncomplete Batches : ");
		printBatches(inCompleteBatches);
	}

	@Getter
	@Setter
	@ToString
	@NoArgsConstructor
	@AllArgsConstructor
	public static class KredxBatch {
		public int batchSize = 0;
		public Double batchTotal = 0.0;
		public List<Double> batch = new ArrayList<>();
	}
}
