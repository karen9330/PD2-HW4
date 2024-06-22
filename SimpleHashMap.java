class HashNode<K, V> {
    K key;
    V value;
    HashNode<K, V> next;

    public HashNode(K key, V value) {
        this.key = key;
        this.value = value;
    }
}

public class SimpleHashMap<K, V> {
    //數組存儲鏈表頭部（每個鏈表代表一個哈希表）
    private HashNode<K, V>[] buckets;
    private int capacity;  // 哈希表的容量
    private int size;      // 哈希表的當前大小

    public SimpleHashMap() {
        this(16);  // 默認大小＝16
    }

    public SimpleHashMap(int capacity) {
        this.capacity = capacity;
        this.buckets = new HashNode[capacity];
        this.size = 0;
    }

    private int getBucketIndex(K key) {
        int hashCode = key.hashCode();
        int index = hashCode % capacity;
        return index < 0 ? index + capacity : index;
    }

    public void put(K key, V value) {
        if (key == null) return;  // 不允許儲存null

        int bucketIndex = getBucketIndex(key);
        HashNode<K, V> head = buckets[bucketIndex];

        // key有沒有存在
        while (head != null) {
            if (head.key.equals(key)) {
                head.value = value;
                return;
            }
            head = head.next;
        }

        // 插入keySet到頂部
        size++;
        head = buckets[bucketIndex];
        HashNode<K, V> newNode = new HashNode<>(key, value);
        newNode.next = head;
        buckets[bucketIndex] = newNode;

        // 如果需要，擴充默認大小
        if ((1.0 * size) / capacity >= 0.7) {
            resize();
        }
    }

    public V get(K key) {
        int bucketIndex = getBucketIndex(key);
        HashNode<K, V> head = buckets[bucketIndex];

        // 搜索key
        while (head != null) {
            if (head.key.equals(key)) {
                return head.value;
            }
            head = head.next;
        }

        return null;  // 如果未找到key
    }

    private void resize() {
        HashNode<K, V>[] oldBuckets = buckets;
        capacity *= 2;
        buckets = new HashNode[capacity];
        size = 0;

        for (HashNode<K, V> headNode : oldBuckets) {
            while (headNode != null) {
                put(headNode.key, headNode.value);
                headNode = headNode.next;
            }
        }
    }

    public int size() {
        return size;
    }
}
