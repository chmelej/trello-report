package net.czela.trello

import java.text.SimpleDateFormat

public class MediaWikiFormater {
    SimpleDateFormat fmtDate = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    String head = 'no head???'

    Map<String, String> report = [:]

    Map<String, Integer> sectionNameOrder = [
            "Nové"          : 10,
            "Schváleno"     : 20,
            "Podpora zařídí": 30,
            "Běží"          : 31,
            "Čekáme"        : 40,
            "Hotovo"        : 90,
            "Zamítnuto"     : 100,

            "Nové body na další sezení Rady": 11,
            "ŘEŠÍ SE ..."                   : 21,
            "Archiv"                        : 90,
    ]

    int sectionOrderPrio = 50;
    Map<String, Integer> sectionOrder = [:]

    def addSection(def id, def label) {
        Integer prio = sectionNameOrder.get(label)
        if (prio == null) {
            prio = sectionOrderPrio++;
        }
        sectionOrder.put(id, prio);
        report.put(id, "== $label ==\n\n")
    }

    def addSubsection(def parentId, def label, def body) {
        def section = report.get(parentId)
        if (section == null) section = "== Unknown ==\n\n"
        section += "=== $label ===\n\n$body\n\n"
        report.put(parentId, section)
    }

    def addHead(def body) {
        head = "$body\n\n"
    }

    def fmtCustomKV(def key, def value) {
        return "* '''$key''': $value\n"
    }

    def fmtNote(Date date, String note) {
        String fmtd = fmtDate.format(date)
        if (note.contains("\n")) {
            String prefix = note.startsWith('From:')?'   ':'*** '
            note = note.split(/\n/).collect()
                    .grep({ it ==~ /.*\S.*/}).collect({"$prefix$it".replaceAll(/\*\s+\*/,'**')})
                    .join("\n")
            return "** $fmtd :\n${note}\n"
        } else {
            return "** $fmtd - ${note}\n"
        }
    }

    def printReport(boolean finalVersion) {
        def sortedSectionIds = sortByValue(sectionOrder)
        def buf = new StringBuilder()
        if (!finalVersion) {
            buf.append("Draft: ")
        }
        buf.append(head)
        for (String key : sortedSectionIds) {
            if (!report.get(key).endsWith(" ==\n\n")) { // preskoc prazdne panely
                buf.append(report.get(key) + "\n\n")
            }
        }
        return buf.toString().replaceFirst(/\s+$/,'').replaceAll(/\n\n\n+/,"\n\n");
    }

    private static List<String> sortByValue(Map<String, Integer> unsortMap) {

        // 1. Convert Map to List of Map
        List<Map.Entry<String, Integer>> list =
                new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        ArrayList<String> sortedList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedList.add(entry.getKey());
        }

        return sortedList;
    }
}
