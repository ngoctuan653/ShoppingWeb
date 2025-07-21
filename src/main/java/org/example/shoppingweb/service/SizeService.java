package org.example.shoppingweb.service;

import org.example.shoppingweb.entity.Size;
import org.example.shoppingweb.repository.SizeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SizeService {
    @Autowired
    private SizeRepository sizeRepository;

    public Size findByLabel(String label) {
        return sizeRepository.findBySizeLabel(label).orElse(null);
    }

    public Size save(Size size) {
        return sizeRepository.save(size);
    }
}
