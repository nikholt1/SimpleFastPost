package org.example.simplefastpost.config;

import org.example.simplefastpost.model.Command;
import org.example.simplefastpost.model.Folder;
import org.example.simplefastpost.repository.CommandRepository;
import org.example.simplefastpost.repository.FolderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

//
//@Component
//public class InitData implements CommandLineRunner {
//
//    private final FolderRepository folderRepository;
//    private final CommandRepository commandRepository;
//
//    public InitData(FolderRepository folderRepository1, CommandRepository commandRepository1) {
//        this.folderRepository = folderRepository1;
//        this.commandRepository = commandRepository1;
//
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        Folder folder1 = new Folder();
//        folder1.setProjectName("starterFolder");
//
//        Command command1 = new Command();
//        command1.setName("callApiGET");
//        command1.setCommand("-m GET -u localhost:8080/api/folders");
//        command1.setFolderId(1L);
//        command1.setFolder(folder1);
//        Command command2 = new Command();
//        command2.setName("callAPIPOST");
//        command2.setCommand("-m POST -u localhost:8080/api/folder/1");
//        command2.setBody("{" +
//                "'name':'createdCommand'" +
//                "'command':'-m GET -u localhost:8080/commands'" +
//                "}");
//        command2.setFolder(folder1);
//
//        folderRepository.save(folder1);
//        commandRepository.saveAll(List.of(command1, command2));
//
//    }
//}
//
//
