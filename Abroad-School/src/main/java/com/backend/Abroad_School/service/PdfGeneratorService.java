package com.backend.Abroad_School.service;

import com.backend.Abroad_School.model.Student;
import java.io.ByteArrayInputStream;
public interface PdfGeneratorService {
ByteArrayInputStream generateAdmissionVoucher(Student student, double admissionFee);
ByteArrayInputStream generateFeeReceipt(/* params */);
}

