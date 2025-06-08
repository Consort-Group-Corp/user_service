package uz.consortgroup.userservice.service.processor;

import java.util.List;

public interface ActionProcessor<T> {
    void process(List<T> events);
}